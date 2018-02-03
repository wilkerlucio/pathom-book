(ns com.wsscode.pathom.book.app-types
  (:require [fulcro.client.primitives :as fp]
            [fulcro.client :as fulcro]))

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
(defonce app-types (atom {}))

(defn register-app [name f]
  (swap! app-types assoc name f))

(defn update-apps []
  (doseq [[id {::keys [root node]}] @apps]
    (swap! apps update-in [id ::app] fulcro/mount root node)))

(defn mount-app [name node]
  (if-let [app-factory (get @app-types name)]
    (let [id  (random-uuid)
          {::keys [root app]} (app-factory)
          app (or app (fulcro/new-fulcro-client))]
      (swap! apps assoc id {::app app ::root root ::node node}))
    (js/console.warn "App type" name "is not registered")))
