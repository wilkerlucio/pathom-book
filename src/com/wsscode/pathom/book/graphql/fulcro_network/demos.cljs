(ns com.wsscode.pathom.book.graphql.fulcro-network.demos
  (:require [com.wsscode.pathom.book.app-types :as app-types]
            [com.wsscode.pathom.book.graphql.fulcro-network.name-query :as demo.name-query]
            [com.wsscode.pathom.book.util.local-storage :as ls]
            [fulcro.client :as fulcro]
            [fulcro.client.dom :as dom]
            [fulcro.client.primitives :as fp]
            [goog.object :as gobj]
            [fulcro.client.mutations :as mutations]
            [fulcro-css.css :as css]))

(defn start-app [this node token]
  (let [{::keys [app root make-app]} (fp/shared this)]
    (reset! app (fulcro/mount (make-app token) root node))))

(fp/defsc RequireTokenApp
  [this {::keys [token]} _ css]
  {:initial-state     {::token (ls/get ::token)}
   :ident             (fn [] [::token-app "singleton"])
   :query             [::token]
   :componentDidMount (fn []
                        (if-let [token (-> this fp/props ::token)]
                          (start-app this (gobj/get this "container") token)))
   :css               [[:.container {:border  "6px solid #000"
                                     :padding "10px"}]]}
  (dom/div #js {:className (:container css)}
    (if-not token
      (dom/button #js {:onClick #(when-let [token (ls/get ::token (js/prompt "Input your github personal token:"))]
                                   (ls/set! ::token token)
                                   (mutations/set-value! this ::token token)
                                   (start-app this (gobj/get this "container") token))}
        "Input/restore token"))
    (dom/div #js {:ref #(gobj/set this "container" %)})))

(app-types/register-app "demo-fulcro-network-name-query"
  (fn []
    {::app-types/app  (fulcro/new-fulcro-client :shared {::make-app demo.name-query/make-app
                                                         ::app      (atom nil)
                                                         ::root     (app-types/make-root demo.name-query/GithubUserView "demo-fulcro-network-name-query")})
     ::app-types/root (app-types/make-root RequireTokenApp "demo-fulcro-network-name-query-container")}))

(css/upsert-css "token-demo" RequireTokenApp)
