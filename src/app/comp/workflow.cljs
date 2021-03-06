
(ns app.comp.workflow
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp <> action-> cursor-> mutation-> list-> span div input button a]]
            [respo.comp.space :refer [=<]]
            [clojure.string :as string]
            [app.style :as style]
            [app.util :refer [map-val]]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [feather.core :refer [comp-i]]
            [app.comp.command :refer [comp-command-editor comp-command-row]]
            [respo-alerts.comp.alerts :refer [comp-confirm]]))

(defcomp
 comp-workflow-editor
 (states base-workflow on-toggle)
 (let [state (or (:data states)
                 (if (some? base-workflow)
                   (select-keys base-workflow [:name :base-dir])
                   {:name "", :base-dir "./"}))]
   (div
    {}
    (div {} (<> "Workflow" {:font-family ui/font-fancy}))
    (=< nil 6)
    (div
     {}
     (input
      {:style (merge ui/input {:width 240}),
       :placeholder "Workflow name",
       :value (:name state),
       :on-input (mutation-> (assoc state :name (:value %e)))}))
    (=< nil 8)
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
           (if (some? base-workflow)
             (d! :workflow/edit (assoc data :id (:id base-workflow)))
             (d! :workflow/create data))
           (m! nil)
           (on-toggle m!)))}
      (<> "Submit"))))))

(defcomp
 comp-workflow-detail
 (states workflow)
 (div
  {}
  (div
   {:style (merge ui/row-parted)}
   (div
    {:style ui/row-middle}
    (<>
     "Commands"
     {:font-size 24, :font-family ui/font-fancy, :color (hsl 0 0 70), :font-weight 100})
    (=< 8 nil)
    (<> (:base-dir workflow) {:font-family ui/font-code, :color (hsl 0 0 70)})
    (=< 40 nil)
    (cursor->
     :add
     comp-popup
     states
     {:trigger (comp-i :plus 16 (hsl 200 80 60))}
     (fn [on-toggle]
       (cursor->
        :add-command
        comp-command-editor
        states
        nil
        (fn [command-draft d! m!]
          (d! :workflow/add-command {:workflow-id (:id workflow), :draft command-draft})
          (on-toggle m!))))))
   (div
    {:style ui/row-parted}
    (cursor->
     :edit
     comp-popup
     states
     {:trigger (comp-i :edit-2 14 (hsl 200 80 60)), :style {:display :inline-block}}
     (fn [on-toggle] (cursor-> :editor comp-workflow-editor states workflow on-toggle)))
    (=< 8 nil)
    (cursor->
     :remove
     comp-confirm
     states
     {:trigger (comp-i :x 18 (hsl 0 80 60))}
     (fn [e d! m!] (d! :workflow/remove (:id workflow))))))
  (list->
   {}
   (->> (:commands workflow)
        (map
         (fn [[k command]] [k (cursor-> k comp-command-row states command (:id workflow))]))))))

(def style-workflow-entry
  {:cursor :pointer,
   :padding "0 8px",
   :min-width 40,
   :min-height 20,
   :border-bottom (str "1px solid " (hsl 0 0 94 0.1)),
   :line-height "36px"})

(defcomp
 comp-workflow-container
 (states workflows)
 (let [state (or (:data states) {:focused-id nil, :base-workflow nil})]
   (div
    {:style (merge ui/flex ui/row {:padding 16})}
    (div
     {:style {:width 200}}
     (div
      {:style ui/row-parted}
      (<> "Workflows" {:font-family ui/font-fancy})
      (cursor->
       :add-workflow
       comp-popup
       states
       {:trigger (comp-i :plus 16 (hsl 200 80 60))}
       (fn [on-toggle] (cursor-> :editor comp-workflow-editor states nil on-toggle))))
     (=< nil 8)
     (list->
      {}
      (->> workflows
           (sort-by (fn [[k workflow]] (:name workflow)))
           (map-val
            (fn [workflow]
              (div
               {:style (merge
                        style-workflow-entry
                        {:background-color (if (= (:id workflow) (:focused-id state))
                           (hsl 0 0 100 0.2)
                           (hsl 0 0 100 0))}),
                :on-click (mutation-> (assoc state :focused-id (:id workflow)))}
               (<> (:name workflow))))))))
    (div {:style {:width 1, :background-color (hsl 0 0 100 0.2), :margin 16}})
    (div
     {:style (merge ui/flex {:padding 8})}
     (let [focused-id (:focused-id state)]
       (if (and (some? focused-id) (some? (get workflows focused-id)))
         (let [workflow (get workflows focused-id)]
           (cursor-> :detail comp-workflow-detail states workflow))
         (div
          {}
          (<>
           "Nothing"
           {:font-family ui/font-fancy,
            :color (hsl 0 0 70),
            :font-size 20,
            :font-weight 100}))))))))
