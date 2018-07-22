(ns learnclojure.chapt4.core10
  (:require [net.cgrand.enlive-html :as ehtml]
            [clojure.java.io :as io])
  (:use [clojure.string :only [lower-case]])
  (:import [java.net URL MalformedURLException]
           (java.util.concurrent LinkedBlockingQueue BlockingQueue)
           (java.io StringReader)))

;;使用agent把操作并行化,以一个简单的爬虫为例子
(defn- links-from
  "抽取出网页中的超链接"
  [base-url html]
  (remove nil?
          (for [link (ehtml/select html [:a])]
            (when-let [href (-> link :attrs :href)]
              (try
                (URL. base-url href)
                (catch MalformedURLException _))))))
(defn- words-from
  "抽取出网页中的文本标签中的非纯数字的单词"
  [html]
  (let [chunks (-> html
                   (ehtml/at [:script] nil)
                   (ehtml/select [:body ehtml/text-node]))]
    (->> chunks
         (mapcat (partial re-seq #"\w+"))
         (remove (partial re-matches #"\d+"))
         (map lower-case))))
(def url-queue
  "等待爬取的url队列"
  (LinkedBlockingQueue.))
(def crawled-urls
  "已经爬取过的url的集合"
  (atom #{}))
(def word-freqs
  "单词以及对应的出现次数的map"
  (atom {}))

(declare get-url)
;;定义运行任务的agent,其中的::t是需要运行的任务函数
(def agents (set (repeatedly 25 #(agent {::t #'get-url :queue url-queue}))))

(declare run process handle-results)
(defn ^::blocking get-url
  "从队列中拿出一个url,并获取它的内容,传给下一个步骤"
  [{:keys [^BlockingQueue queue] :as state}]
  (let [url (io/as-url (.take queue))]
    (try
      (if (@crawled-urls url)
        state
        {:url     url
         :content (slurp url)
         ::t      #'process})
      (catch Exception e
        ;;略过任何获取内容失败的url
        state)
      ;;*agent*指向的是当前这个agent,类似this
      (finally (run *agent*)))))
(defn process
  "对url的内容进行解析,调用links-from和words-from,并把它们的返回值传到下一个步骤"
  [{:keys [url content]}]
  (try
    (let [html (ehtml/html-resource (StringReader. content))]
      {::t    #'handle-results
       :url   url
       :links (links-from url html)
       :words (reduce #(update %1 %2 (fnil inc 0))
                      {}
                      (words-from html))})
    (finally (run *agent*))))
(defn ^::blocking handle-results
  "处理结果,把当前的url放入crawled-urls,把抽取到的url放入url-queue,把该页面的单词出现数的统计数据合并入word-freqs"
  [{:keys [url links words]}]
  (try
    (swap! crawled-urls conj url)
    (doseq [url links]
      (.put url-queue url))
    (swap! word-freqs (partial merge-with +) words)
    {::t #'get-url :queue url-queue}
    (finally (run *agent*))))
(defn paused?
  "判断agent是否暂停"
  [agent]
  (::paused (meta agent)))
(defn run
  ([] (doseq [agent agents] (run agent)))
  ([a]
   (when (agents a)
     (send a (fn [{transition ::t :as state}]
               (when-not (paused? *agent*)
                 (let [dispatch-fn (if (-> transition meta ::blocking)
                                     send-off
                                     send)]
                   (dispatch-fn *agent* transition)
                   state)))))))
(defn pause
  ([] (doseq [a agents] (pause a)))
  ([a] (alter-meta! a assoc ::paused true)))
(defn restart
  ([] (doseq [a agents] (restart a)))
  ([a]
   (alter-meta! a dissoc ::paused)
   (run a)))
(defn test-crawler
  [agent-count starting-url]
  (def agents (set (repeatedly agent-count #(agent {::t #'get-url :queue url-queue}))))
  (.clear url-queue)
  (swap! crawled-urls empty)
  (swap! word-freqs empty)
  (.add url-queue starting-url)
  (run)
  (Thread/sleep 60000)
  (pause)
  [(count @crawled-urls) (count @word-freqs)])
(test-crawler 25 "http://www.ccb.com/cn/home/indexv3.html")

;;使用java的并发api
(.start (Thread. #(println "Running")))

;locking,用于获取对象的锁,在离开locking的时候锁会自动释放,基本上等价于synchronized
(defn add
  [^java.util.List some-list value]
  (locking some-list
    (.add some-list value)))