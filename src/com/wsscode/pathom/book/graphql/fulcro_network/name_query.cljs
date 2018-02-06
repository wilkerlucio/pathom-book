(ns com.wsscode.pathom.book.graphql.fulcro-network.name-query
  (:require [com.wsscode.pathom.fulcro.network :as pfn] ; <1>
            [fulcro.client :as fulcro]
            [fulcro.client.dom :as dom]
            [fulcro.client.data-fetch :as fetch]
            [fulcro.client.primitives :as fp]))

(fp/defsc GithubUserView
  [_ {:keys [name]}]
  {:query [:id :name] ; <2>
   :ident [:id :id]}
  (dom/div nil
    (str "My Github name is: " name)))

(def github-user-view (fp/factory GithubUserView {:keyfn :id}))

(fp/defsc Root
  [_ {:keys [viewer]}]
  {:query [{:viewer (fp/get-query GithubUserView)}]} ; <3>
  (github-user-view viewer))

(defn make-app [token]
  (fulcro/new-fulcro-client
    :started-callback
    (fn [{:keys [reconciler]}]
      (fetch/load reconciler :viewer GithubUserView {:target [:ui/root]})) ; <4>

    :networking
    {:remote
     (pfn/graphql-network ; <5>
       {::pfn/url (str "https://api.github.com/graphql?access_token=" token)})}))

