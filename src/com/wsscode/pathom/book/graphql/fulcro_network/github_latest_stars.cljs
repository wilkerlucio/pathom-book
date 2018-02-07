(ns com.wsscode.pathom.book.graphql.fulcro-network.github-latest-stars
  (:require [fulcro.client.dom :as dom]
            [fulcro.client.primitives :as fp]
            [fulcro.client :as fulcro]
            [com.wsscode.pathom.fulcro.network :as pfn]
            [fulcro.client.data-fetch :as df]))

(fp/defsc Repository
  [this {:github.repository/keys [name-with-owner]}]
  {:ident [:github.repository/id :github.repository/id]
   :query [:github.repository/id
           :github.repository/name-with-owner]}
  (dom/div nil
    (str name-with-owner)))

(def repository (fp/factory Repository {:keyfn :github.repository/id}))

(fp/defsc LastestStarred
  [this {:keys [github/starred-repositories] :as props} _ css]
  {:initial-state (fn [_] {})
   :ident         (fn [] [::starred "singleton"])
   :query         (fn []
                    [(list {:github/starred-repositories
                            [{:nodes (fp/get-query Repository)}]}
                       '{:first    10
                         :order-by {:field     STARRED_AT
                                    :direction DESC}})
                     [df/marker-table ::loading]])
   :css           [[:.title {:margin-bottom "8px"
                             :font-weight   "bold"}]]}
  (let [marker (get props [df/marker-table ::loading])]
    (dom/div nil
      (cond
        starred-repositories
        (dom/div nil
          (dom/div #js {:className (:title css)} "The last repositories you added a star to:")
          (map repository (:nodes starred-repositories)))

        (df/loading? marker)
        "Loading..."

        :else
        (dom/button #js {:onClick
                         #(df/load this :viewer LastestStarred {:target [:ui/root]
                                                                :marker ::loading})}
          "Load latest starred repositories")))))

(defn new-client [token]
  (fulcro/new-fulcro-client
    :networking
    {:remote
     (pfn/graphql-network
       {::pfn/url (str "https://api.github.com/graphql?access_token=" token)})}))
