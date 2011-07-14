(ns clipper.demo
  (:import [org.llrp.ltk.net LLRPConnector LLRPEndpoint]
           [org.llrp.ltk.generated.messages
            RO_ACCESS_REPORT])
  (:require [clipper.core :as clipper]))

;;; This is a demo of how an application might use the clipper
;;; library. At the moment there are no options. The only thing that
;;; the library allows you to do is create the reader with the correct
;;; hostname/ip and supply the tag-read handler (which obviously can
;;; do application-specific stuff). Then calling (clipper/start
;;; reader) will start the reader, and calling (clipper/stop reader)
;;; will stop it again.

(def handler
     (proxy [Object LLRPEndpoint] []
       (errorOccured [msg] nil)
       (messageReceived [msg]
                        (if (= (.getTypeNum msg) RO_ACCESS_REPORT/TYPENUM)
                          (let [tags (.getTagReportDataList msg)]
                            (doseq [tag tags]
                              (println (.getEPCParameter tag))
                              (println (.getLastSeenTimestampUTC tag))))))))

(def reader (LLRPConnector. handler "10.2.0.99"))

(defn run []
  (clipper/start reader)
  (Thread/sleep 10000)
  (clipper/stop reader))
