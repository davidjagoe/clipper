(ns clipper.core
  (:import [org.llrp.ltk.net LLRPConnector LLRPEndpoint]
           [org.llrp.ltk.types LLRPMessage UnsignedInteger UnsignedByte UnsignedShortArray]
           [org.llrp.ltk.generated.messages
            RO_ACCESS_REPORT
            DELETE_ROSPEC]
           [org.llrp.ltk.generated.enumerations
            ROSpecState
            ROSpecStartTriggerType
            ROSpecStopTriggerType
            AISpecStopTriggerType
            ]
           [org.llrp.ltk.generated.parameters
            AISpec
            AISpecStopTrigger
            ROSpec
            ROBoundarySpec
            ROSpecStartTrigger
            ROSpecStopTrigger
            
            ]))

;; Translating the following example from java to clojure.
;; http://learn.impinj.com/2010/09/creating-rfid-applications-with-java.html

(def TIMEOUT_MS 10000)
(def ROSPEC_ID 123)

(def handler
     (proxy [Object LLRPEndpoint] []
       (errorOccured [msg] nil)
       (messageReceived [msg]
                        (if (= (.getTypeNum msg)) RO_ACCESS_REPORT/TYPENUM
                            (println msg)))))

(def reader (LLRPConnector. handler "10.2.0.99"))

(defn connect [reader]
  (.connect reader))

(defn delete-ro-specs [reader]
  (println "Deleting all ROSpecs")
  (let [del
        (doto (DELETE_ROSPEC.)
          (.setROSpecID (UnsignedInteger. 0)))]
    (println (.toXMLString (.transact reader del TIMEOUT_MS)))))

(defn add-ro-spec [reader]
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
          (.setAISpecStopTriggerType (AISpecStopTriggerType. AISpecStopTriggerType/Null))))
       (.setAntennaIDs
        (doto (UnsignedShortArray.)
          (.add (UnsignedInteger. 0)))))
     ))

  )

(defn disconnect [reader]
  (.disconnect reader))

(connect reader)
(delete-ro-specs reader)
(disconnect reader)
(add-ro-spec reader)
;; (enable-ro-specs)
;; (start-ro-specs)

;; (def config-xml "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
;; <llrp:ADD_ROSPEC xmlns:llrp=\"http://www.llrp.org/ltk/schema/core/encoding/xml/1.0\" Version=\"1\" MessageID=\"4\">
;;   <llrp:ROSpec>
;;     <llrp:ROSpecID>1</llrp:ROSpecID>
;;     <llrp:Priority>0</llrp:Priority>
;;     <llrp:CurrentState>Disabled</llrp:CurrentState>
;;     <llrp:ROBoundarySpec>
;;       <llrp:ROSpecStartTrigger>
;;         <llrp:ROSpecStartTriggerType>Null</llrp:ROSpecStartTriggerType>
;;       </llrp:ROSpecStartTrigger>
;;       <llrp:ROSpecStopTrigger>
;;         <llrp:ROSpecStopTriggerType>Null</llrp:ROSpecStopTriggerType>
;;         <llrp:DurationTriggerValue>0</llrp:DurationTriggerValue>
;;       </llrp:ROSpecStopTrigger>
;;     </llrp:ROBoundarySpec>
;;     <llrp:AISpec>
;;       <llrp:AntennaIDs>0</llrp:AntennaIDs>
;;       <llrp:AISpecStopTrigger>
;;         <llrp:AISpecStopTriggerType>Null</llrp:AISpecStopTriggerType>
;;         <llrp:DurationTrigger>0</llrp:DurationTrigger>
;;       </llrp:AISpecStopTrigger>
;;       <llrp:InventoryParameterSpec>
;;         <llrp:InventoryParameterSpecID>9</llrp:InventoryParameterSpecID>
;;         <llrp:ProtocolID>EPCGlobalClass1Gen2</llrp:ProtocolID>
;;       </llrp:InventoryParameterSpec>
;;     </llrp:AISpec>
;;     <llrp:ROReportSpec>
;;       <llrp:ROReportTrigger>Upon_N_Tags_Or_End_Of_AISpec</llrp:ROReportTrigger>
;;       <llrp:N>1</llrp:N>
;;       <llrp:TagReportContentSelector>
;;         <llrp:EnableROSpecID>1</llrp:EnableROSpecID>
;;         <llrp:EnableSpecIndex>1</llrp:EnableSpecIndex>
;;         <llrp:EnableInventoryParameterSpecID>1</llrp:EnableInventoryParameterSpecID>
;;         <llrp:EnableAntennaID>1</llrp:EnableAntennaID>
;;         <llrp:EnableChannelIndex>1</llrp:EnableChannelIndex>
;;         <llrp:EnablePeakRSSI>1</llrp:EnablePeakRSSI>
;;         <llrp:EnableFirstSeenTimestamp>1</llrp:EnableFirstSeenTimestamp>
;;         <llrp:EnableLastSeenTimestamp>1</llrp:EnableLastSeenTimestamp>
;;         <llrp:EnableTagSeenCount>1</llrp:EnableTagSeenCount>
;;         <llrp:EnableAccessSpecID>1</llrp:EnableAccessSpecID>
;;         <llrp:C1G2EPCMemorySelector>
;;           <llrp:EnableCRC>1</llrp:EnableCRC>
;;           <llrp:EnablePCBits>1</llrp:EnablePCBits>
;;         </llrp:C1G2EPCMemorySelector>
;;       </llrp:TagReportContentSelector>
;;     </llrp:ROReportSpec>
;;   </llrp:ROSpec>
;; </llrp:ADD_ROSPEC>")

;; (def config-dom (org.jdom.Document. config-xml))

;; (def config-message (LLRPMessage.))
;; (def config-dom ())



