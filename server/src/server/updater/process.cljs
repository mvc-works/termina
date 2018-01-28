
(ns server.updater.process (:require [server.schema :as schema]))

(defn create [db op-data sid op-id op-time]
  (assoc-in db [:processes op-id] (merge schema/process op-data {:started-at op-time})))

(defn kill [db op-data sid op-id op-time] (assoc-in db [:processes op-data :alive?] false))

(defn stderr [db op-data sid op-id op-time]
  (update-in
   db
   [:processes (:pid op-data) :content]
   (fn [content] (conj content {:type :stderr, :data (:data op-data)}))))

(defn stdout [db op-data sid op-id op-time]
  (update-in
   db
   [:processes (:pid op-data) :content]
   (fn [content] (conj content {:type :stdout, :data (:data op-data)}))))