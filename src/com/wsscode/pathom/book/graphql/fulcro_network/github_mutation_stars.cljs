(ns com.wsscode.pathom.book.graphql.fulcro-network.github-mutation-stars
  (:require [clojure.string :as str]
            [com.wsscode.pathom.fulcro.network :as pfn]
            [fulcro.client :as fulcro]
            [fulcro.client.dom :as dom]
            [fulcro.client.mutations :as mutations]
            [fulcro.client.primitives :as fp]))

(mutations/defmutation add-star [_]
  (action [{:keys [state ref]}]
    (swap! state update-in ref assoc :github.repository/viewer-has-starred true))
  (remote [_] true))

(fp/defsc StarRepo
  [this {:github.repository/keys [id name name-with-owner viewer-has-starred]} _ css]
  {:initial-state {}
   :ident         (fn []
                    (let [[owner name] (str/split name-with-owner #"/")]
                      [:github.repository/owner-and-name [owner name]]))
   :query         [:github.repository/id
                   :github.repository/name
                   :github.repository/name-with-owner
                   :github.repository/viewer-has-starred]
   :css           [[:.heart {:text-shadow "0 0 0 #2f2f2f"
                             :color "transparent"
                             :transition "text-shadow 300ms"
                             :font-size "28px"}
                    [:&.red {:text-shadow "0 0 0 #f50909"}]]]
   :css-include   []}
  (dom/div nil
    (dom/div #js {:className (str (:heart css) " " (if viewer-has-starred (:red css)))} "❤️")
    (if viewer-has-starred
      (str "Great, thanks for the " name " love!")
      (dom/button #js {:onClick #(fp/transact! this `[{(add-star {:input {:starrable-id ~id}}) [:client-mutation-id]}])}
        (str "Give love to " name)))))

(def star-repo (fp/factory StarRepo {:keyfn :github.repository/name-with-owner}))

(def repos
  [["wilkerlucio" "pathom"]
   ["fulcrologic" "fulcro"]
   ["fulcrologic" "fulcro-inspect"]])

(fp/defsc GithubStars [_ {::keys [repos]}]
  {:initial-state (fn [_]
                    {::repos [#:github.repository{:name-with-owner "wilkerlucio/pathom"}
                              #:github.repository{:name-with-owner "fulcrologic/fulcro"}
                              #:github.repository{:name-with-owner "fulcrologic/fulcro-inspect"}]})
   :ident         (fn [] [::github-stars "singleton"])
   :query         [{::repos (fp/get-query StarRepo)}]
   :css-include   [StarRepo]}
  (dom/div nil
    "Hello Star"
    (if (-> repos first :github.repository/id)
      (map star-repo repos))))

(defn new-client [token]
  (fulcro/new-fulcro-client
    :started-callback
    (fn [{:keys [reconciler]}]
      (fp/transact! reconciler [(list 'fulcro/load {:query   [{[:github.repository/owner-and-name ["wilkerlucio" "pathom"]] (fp/get-query StarRepo)}
                                                              {[:github.repository/owner-and-name ["fulcrologic" "fulcro"]] (fp/get-query StarRepo)}
                                                              {[:github.repository/owner-and-name ["fulcrologic" "fulcro-inspect"]] (fp/get-query StarRepo)}]
                                                    :refresh [::repos]})]))

    :networking
    {:remote
     (pfn/graphql-network
       {::pfn/url (str "https://api.github.com/graphql?access_token=" token)})}))
