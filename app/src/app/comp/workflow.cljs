
(ns app.comp.workflow
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo-ui.colors :as colors]
            [respo.macros
             :refer
             [defcomp <> action-> cursor-> mutation-> list-> span div input button]]
            [respo.comp.space :refer [=<]]
            [clojure.string :as string]
            [app.style :as style]
            [app.comp.dialog :refer [comp-dialog]]))

(defcomp
 comp-command-editor
 (states on-close! workflow-id)
 (let [state (or (:data states) {:code "", :path ""})]
   (div
    {}
    (div
     {}
     (input
      {:style ui/input,
       :value (:code state),
       :placeholder "Command code",
       :on-input (mutation-> (assoc state :code (:value %e)))}))
    (=< nil 16)
    (div
     {}
     (input
      {:style ui/input,
       :value (:path state),
       :placeholder "Command path",
       :on-input (mutation-> (assoc state :path (:value %e)))}))
    (=< nil 16)
    (div
     {:style ui/row-parted}
     (span {})
     (button
      {:style style/button,
       :on-click (fn [e d! m!]
         (d!
          :workflow/add-command
          {:workflow-id workflow-id, :code (:code state), :path (:path state)})
         (m! nil)
         (on-close! m!))}
      (<> "add"))))))

(defcomp
 comp-workflow-detail
 (states workflow)
 (let [state (or (:data states) {:edit-command? false})]
   (div
    {}
    (div
     {:style (merge ui/row {:align-items :center})}
     (<> (:name workflow) {:font-size 24})
     (=< 8 nil)
     (<> (:base-dir workflow) {:font-family ui/font-code, :color (hsl 0 0 70)})
     (=< 16 nil)
     (button
      {:style style/button, :on-click (fn [e d! m!] (m! (assoc state :edit-command? true)))}
      (<> "add"))
     (=< 16 nil)
     (button
      {:style style/button, :on-click (action-> :workflow/remove (:id workflow))}
      (<> "rm")))
    (list->
     {}
     (->> (:commands workflow)
          (map
           (fn [[k command]]
             [k
              (div
               {:style {:font-family ui/font-code}}
               (<> (:code command) {:background-color (hsl 0 0 90), :padding "0 8px"})
               (=< 8 nil)
               (<> (:path command))
               (=< 8 nil)
               (button
                {:style style/button,
                 :on-click (action->
                            :workflow/remove-command
                            {:workflow-id (:id workflow), :id (:id command)})}
                (<> "rm")))]))))
    (if (:edit-command? state)
      (let [on-close! (fn [m!]
                        (println "cursor" %cursor)
                        (m! %cursor (assoc state :edit-command? false)))
            workflow-id (:id workflow)]
        (comp-dialog
         on-close!
         (cursor-> :command comp-command-editor states on-close! workflow-id)))))))

(defcomp
 comp-workflow-editor
 (states on-close!)
 (let [state (or (:data states) {:name "", :base-dir "./"})]
   (println "editor at here")
   (div
    {}
    (div
     {}
     (input
      {:style (merge ui/input {:width 240}),
       :placeholder "Workflow name",
       :value (:name state),
       :on-input (mutation-> (assoc state :name (:value %e)))}))
    (=< nil 16)
    (div
     {}
     (input
      {:style (merge ui/input {:width 240}),
       :placeholder "Base directory",
       :value (:base-dir state),
       :on-input (mutation-> (assoc state :base-dir (:value %e)))}))
    (=< nil 16)
    (div
     {:style ui/row-parted}
     (span {})
     (button
      {:style style/button,
       :on-click (fn [e d! m!]
         (let [data (select-keys state [:name :base-dir])]
           (d! :workflow/create data)
           (m! nil)
           (on-close! m!)))}
      (<> "Create Workflow"))))))

(defcomp
 comp-workflow-container
 (states workflows)
 (let [state (or (:data states) {:focused-id nil, :edit-workflow? false})]
   (div
    {:style (merge ui/row {:padding 16})}
    (div
     {:style {:width 240}}
     (div
      {:style ui/row-parted}
      (<> "Workflows")
      (button
       {:style style/button,
        :on-click (fn [e d! m!] (m! (assoc state :edit-workflow? true)))}
       (<> "add")))
     (list->
      {}
      (->> workflows
           (map
            (fn [[k workflow]]
              [k
               (div
                {:style {:cursor :pointer,
                         :background-color (if (= (:focused-id state) k)
                           (hsl 0 0 80)
                           (hsl 0 0 90)),
                         :margin "4px 0",
                         :padding "0 8px"},
                 :on-click (mutation-> (assoc state :focused-id k))}
                (<> (:name workflow)))])))))
    (=< 16 nil)
    (div
     {:style ui/flex}
     (let [focused-id (:focused-id state)]
       (if (and (some? focused-id) (some? (get workflows focused-id)))
         (cursor-> :detail comp-workflow-detail states (get workflows focused-id))
         (div {} (<> "nothing")))))
    (if (:edit-workflow? state)
      (let [on-close! (fn [m!] (m! %cursor (assoc state :edit-workflow? false)))]
        (comp-dialog on-close! (cursor-> :editor comp-workflow-editor states on-close!)))))))
