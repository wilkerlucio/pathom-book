(ns com.wsscode.pathom.book.main
  (:require [com.wsscode.pathom.book.app-types :as app-types]
            [com.wsscode.pathom.book.graphql.edn-converter]
            [com.wsscode.pathom.book.graphql.fulcro-network.contacts]))

(defn main []
  (doseq [node (array-seq (js/document.querySelectorAll "div[x-app]"))
          :let [app-name (.getAttribute node "x-app")]]
    (app-types/mount-app app-name node))

  (app-types/update-apps))

(main)
