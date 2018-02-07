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
   :css           [[:div [:.button {:display "flex"
                                    :align-items "center"
                                    :margin "14px 0"}]]
                   [:.heart {:text-shadow "0 0 0 #2f2f2f"
                             :color       "transparent"
                             :transition  "text-shadow 300ms"
                             :font-size   "28px"
                             :margin-bottom "-3px"
                             :margin-right "14px"}
                    [:&.red {:text-shadow "0 0 0 #f50909"}]]]
   :css-include   []}
  (dom/div nil
    (dom/button #js {:onClick  #(fp/transact! this `[{(add-star {:input {:starrable-id ~id}}) [:client-mutation-id]}])
                     :disabled viewer-has-starred
                     :className (:button css)}
      (dom/div #js {:className (str (:heart css) " " (if viewer-has-starred (:red css)))} "❤️")
      (if viewer-has-starred
        (dom/div nil "Great, thanks for the " (dom/strong nil name) " love!")
        (dom/div nil "Give love (star) to " (dom/strong nil name))))))

(def star-repo (fp/factory StarRepo {:keyfn :github.repository/name-with-owner}))

(def repos
  [["wilkerlucio" "pathom"]
   ["fulcrologic" "fulcro"]
   ["fulcrologic" "fulcro-inspect"]])

(fp/defsc GithubStars [_ {::keys [repos]}]
  {:initial-state (fn [_]
                    {::repos [#:github.repository{:name-with-owner "wilkerlucio/pathom"}
                              #:github.repository{:name-with-owner "fulcrologic/fulcro"}
                              #:github.repository{:name-with-owner "fulcrologic/fulcro-css"}
                              #:github.repository{:name-with-owner "fulcrologic/fulcro-inspect"}
                              #:github.repository{:name-with-owner "fulcrologic/fulcro-spec"}]})
   :ident         (fn [] [::github-stars "singleton"])
   :query         [{::repos (fp/get-query StarRepo)}]
   :css-include   [StarRepo]}
  (dom/div nil
    "Use the buttons bellow to send love to our favorite UI kit tools!"
    (if (-> repos first :github.repository/id)
      (map star-repo repos))))

(defn new-client [token]
  (fulcro/new-fulcro-client
    :started-callback
    (fn [{:keys [reconciler]}]
      (fp/transact! reconciler [(list 'fulcro/load {:query   [{[:github.repository/owner-and-name ["wilkerlucio" "pathom"]] (fp/get-query StarRepo)}
                                                              {[:github.repository/owner-and-name ["fulcrologic" "fulcro"]] (fp/get-query StarRepo)}
                                                              {[:github.repository/owner-and-name ["fulcrologic" "fulcro-css"]] (fp/get-query StarRepo)}
                                                              {[:github.repository/owner-and-name ["fulcrologic" "fulcro-inspect"]] (fp/get-query StarRepo)}
                                                              {[:github.repository/owner-and-name ["fulcrologic" "fulcro-spec"]] (fp/get-query StarRepo)}]
                                                    :refresh [::repos]})]))

    :networking
    {:remote
     (pfn/graphql-network
       {::pfn/url (str "https://api.github.com/graphql?access_token=" token)})}))
