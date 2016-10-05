(ns fivethreeonern.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [fivethreeonern.migrations :as migrations]
            [fivethreeonern.sqlite :as sql]
            [fivethreeonern.events]
            [fivethreeonern.subs]
            [mount.core :as mount]))

(def ReactNative (js/require "react-native"))

(def sql (js/require "react-native-sqlite-storage"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
       [image {:source logo-img
               :style  {:width 80 :height 80 :margin-bottom 30}}]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(alert "HELLO!")}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press (fn []
                                         (sql/query "CREATE TABLE migrations (id INTEGER);"
                                                    #(js/console.log "Table created.")))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Create table"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press (fn []
                                         (sql/query "SELECT * FROM migrations;"
                                                    #(js/console.log "Result:" (clj->js %))))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Query table"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press (fn []
                                         (sql/query "INSERT INTO migrations (id) VALUES (1);"
                                                    #(js/console.log "Inserted into table.")))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Insert into table"]]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press (fn []
                                         (sql/query "DROP TABLE migrations;"
                                                    #(js/console.log "table dropped.")))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Delete table"]]])))

(defn init []
  (mount/start)
  (migrations/migrate
   (fn []
     (js/console.log "Yay! We did it!")
     (dispatch-sync [:initialize-db])
     (.registerComponent app-registry "fivethreeonern" #(r/reactify-component app-root)))))
