(ns com.wsscode.pathom.book.graphql.fulcro-network.demos
  (:require [com.wsscode.pathom.book.app-types :as app-types]
            [com.wsscode.pathom.book.graphql.fulcro-network.name-query :as demo.name-query]
            [com.wsscode.pathom.book.util.local-storage :as ls]
            [fulcro.client :as fulcro]
            [fulcro.client.dom :as dom]
            [fulcro.client.primitives :as fp]
            [goog.object :as gobj]
            [fulcro.client.mutations :as mutations]))

(defn start-app [this node token]
  (let [{::keys [app root make-app]} (fp/shared this)]
    (reset! app (fulcro/mount (make-app token) root node))))

(fp/defsc RequireTokenApp
  [this {::keys [token]}]
  {:initial-state     {::token (ls/get ::token)}
   :query             [::token]
   :componentDidMount (fn []
                        (if-let [token (-> this fp/props ::token)]
                          (start-app this (gobj/get this "container") token)))}
  (dom/div nil
    (if-not token
      (dom/button #js {:onClick #(when-let [token (js/prompt "Input your github personal token:")]
                                   (ls/set! ::token token)
                                   (mutations/set-value! this ::token token)
                                   (start-app this (gobj/get this "container") token)
                                   (fp/force-root-render! (fp/get-reconciler this)))}
        "Input token"))
    (dom/div #js {:ref #(gobj/set this "container" %)})))

(def require-token-app (fp/factory RequireTokenApp))

(app-types/register-app "demo-fulcro-network-name-query"
  (fn []
    {::app-types/app  (fulcro/new-fulcro-client :shared {::make-app demo.name-query/make-app
                                                         ::app      (atom nil)
                                                         ::root     (app-types/make-root demo.name-query/GithubUserView "demo-fulcro-network-name-query")})
     ::app-types/root RequireTokenApp}))
