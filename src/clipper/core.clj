(ns clipper.core
  (:import [org.llrp.ltk.net LLRPConnector LLRPEndpoint]
           [org.llrp.ltk.types LLRPMessage UnsignedInteger
            UnsignedShort UnsignedByte UnsignedShortArray Bit]
           [org.llrp.ltk.generated.messages
            RO_ACCESS_REPORT
            DELETE_ROSPEC
            ADD_ROSPEC
            ENABLE_ROSPEC
            START_ROSPEC]
           [org.llrp.ltk.generated.enumerations
            ROSpecState
            ROSpecStartTriggerType
            ROSpecStopTriggerType
            AISpecStopTriggerType
            AirProtocols
            ROReportTriggerType
            ]
           [org.llrp.ltk.generated.parameters
            AISpec
            AISpecStopTrigger
            ROSpec
            ROBoundarySpec
            ROSpecStartTrigger
            ROSpecStopTrigger
            InventoryParameterSpec
            ROReportSpec
            TagReportContentSelector
            ]))

;;; Clipper is a clojure library built on top of LLRP. It makes it
;;; easy to configure and control LLRP-compliant RFID readers from a
;;; clojure program. At the moment it doesn't do much: it sets up the
;;; reader with a default reader spec (e.g. read on all antennas,
;;; report on every tag read, etc) and allows the application to
;;; supply a reader object with a tag-read handler.

;; Translating the following example from java to clojure.
;; http://learn.impinj.com/2010/09/creating-rfid-applications-with-java.html

(def TIMEOUT_MS 10000)
(def ROSPEC_ID 123)

(defn connect [reader]
  (.connect reader))

(defn delete-ro-specs [reader]
  (println "Deleting all ROSpecs")
  (let [del
        (doto (DELETE_ROSPEC.)
          (.setROSpecID (UnsignedInteger. 0)))]
    (println (.toXMLString (.transact reader del TIMEOUT_MS)))))

(defn build-ro-spec []
  (doto (ROSpec.) ;; Create a Reader Operation Spec
    (.setPriority (UnsignedByte. 0))
    (.setCurrentState (ROSpecState. ROSpecState/Disabled))
    (.setROSpecID (UnsignedInteger. ROSPEC_ID))
    (.setROBoundarySpec
     ;; Set up start and stop triggers
     (doto (ROBoundarySpec.)
       ;; Set the start trigger to null. 
       ;; This means the ROSpec will start as soon as it is enabled
       (.setROSpecStartTrigger
        (doto (ROSpecStartTrigger.)
          (.setROSpecStartTriggerType (ROSpecStartTriggerType. ROSpecStartTriggerType/Null))))
       ;; Set the stop trigger is null. This means the ROSpec will keep
       ;; running until an STOP_ROSPEC message is sent.
       (.setROSpecStopTrigger
        (doto (ROSpecStopTrigger.)
          (.setDurationTriggerValue (UnsignedInteger. 0))
          (.setROSpecStopTriggerType (ROSpecStopTriggerType. ROSpecStopTriggerType/Null))))))
    (.addToSpecParameterList
     ;; Add Antenna Inventory Spec
     (doto (AISpec.)
       ;; Set stop trigger to null which means that the AI spec will
       ;; run until the RO Spec stops.
       (.setAISpecStopTrigger
        (doto (AISpecStopTrigger.)
          (.setAISpecStopTriggerType (AISpecStopTriggerType. AISpecStopTriggerType/Null))
          (.setDurationTrigger (UnsignedInteger. 0))))
       (.setAntennaIDs
        (doto (UnsignedShortArray.)
          (.add (UnsignedShort. 0))))
       ;; Tell the reader that we're reading Gen2 tags
       (.addToInventoryParameterSpecList
        (doto (InventoryParameterSpec.)
          (.setProtocolID (AirProtocols. AirProtocols/EPCGlobalClass1Gen2))
          (.setInventoryParameterSpecID (UnsignedShort. 1))))))
    (.setROReportSpec
     (doto (ROReportSpec.)
       (.setROReportTrigger
        (ROReportTriggerType. ROReportTriggerType/Upon_N_Tags_Or_End_Of_ROSpec))
       (.setN (UnsignedShort. 1))
       (.setTagReportContentSelector
        (doto (TagReportContentSelector.)
          (.setEnableAccessSpecID (Bit. 0))
          (.setEnableAntennaID (Bit. 0))
          (.setEnableChannelIndex (Bit. 0))
          (.setEnableFirstSeenTimestamp (Bit. 0))
          (.setEnableInventoryParameterSpecID (Bit. 0))
          (.setEnableLastSeenTimestamp (Bit. 1))
          (.setEnablePeakRSSI (Bit. 0))
          (.setEnableROSpecID (Bit. 0))
          (.setEnableSpecIndex (Bit. 0))
          (.setEnableTagSeenCount (Bit. 0))))))))

(defn add-ro-spec [reader]
  (let [ro-spec (build-ro-spec)
        message (doto (ADD_ROSPEC.)
                  (.setROSpec ro-spec))]
    (.transact reader message TIMEOUT_MS)))

(defn enable-ro-spec [reader]
  (let [message (doto (ENABLE_ROSPEC.)
                  (.setROSpecID (UnsignedInteger. ROSPEC_ID)))]
    (.transact reader message TIMEOUT_MS)))

(defn start-ro-spec [reader]
  (let [message (doto (START_ROSPEC.)
                  (.setROSpecID (UnsignedInteger. ROSPEC_ID)))]
    (.transact reader message TIMEOUT_MS))
  )

(defn disconnect [reader]
  (.disconnect reader))

;;; Public API
;;; ----------

(defn start [reader]
  (connect reader)
  (delete-ro-specs reader)
  (add-ro-spec reader)
  (enable-ro-spec reader)
  (start-ro-spec reader))

(defn stop [reader]
  (delete-ro-specs reader)
  (disconnect reader))
