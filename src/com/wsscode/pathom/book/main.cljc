(ns com.wsscode.pathom.book.main
  (:require [fulcro.client :as fulcro]
            [fulcro.client.primitives :as fp]
            [com.wsscode.pathom.book.graphql.fulcro-network.contacts :as contacts]))

(defn make-root [{::keys [Root]}]
  (fp/ui
    static fp/InitialAppState
    (initial-state [_ params] {:ui/root (fp/get-initial-state Root params)})

    static fp/IQuery
    (query [_] [{:ui/root (fp/get-query Root)}])

    Object
    (render [this]
      (let [{:ui/keys [root]} (fp/props this)
            factory (fp/factory Root)]
        (factory root)))))

(defonce apps (atom {}))

(def app-map
  {"contacts" contacts/ContactsApp})

(defn update-apps []
  (doseq [[id {::keys [root node]}] @apps]
    (swap! apps update-in [id ::app] fulcro/mount root node)))

(defn main []
  (doseq [node (array-seq (js/document.querySelectorAll "div[x-app]"))
          :let [app-name (.getAttribute node "x-app")]]
    (if-let [app-root (app-map app-name)]
      (let [id   (random-uuid)
            Root (make-root #::{:Root app-root})
            app  (as-> (fp/get-initial-state Root {}) <>
                   (fulcro/new-fulcro-client :initial-state <>))]
        (swap! apps assoc id {::app app ::root Root ::node node}))))

  (update-apps))

(main)
