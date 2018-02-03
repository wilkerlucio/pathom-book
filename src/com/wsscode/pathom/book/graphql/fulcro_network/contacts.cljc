(ns com.wsscode.pathom.book.graphql.fulcro-network.contacts
  (:require [fulcro.client.primitives :as fp]
            [fulcro.client.dom :as dom]))



(fp/defsc ContactsApp
  [this {::keys []} _ css]
  {:initial-state {}
   :ident         [::id ::id]
   :query         [::id]
   :css           []
   :css-include   []}
  (dom/div nil
    "Hello Contacts App World!!"))

(def contacts-app (fp/factory ContactsApp))
