(ns com.wsscode.pathom.book.graphql.fulcro-network.contacts
  (:require [fulcro.client.primitives :as fp]
            [fulcro.client.dom :as dom]
            [com.wsscode.pathom.book.app-types :as app-types]))

(fp/defsc ContactsApp
  [this {::keys []} _ css]
  {:initial-state {::id (random-uuid)}
   :ident         [::id ::id]
   :query         [::id]
   :css           []
   :css-include   []}
  (dom/div nil
    "Hello Contacts App World!!"))

(def contacts-app (fp/factory ContactsApp))

(app-types/register-app "contacts"
  (fn []
    {::app-types/root (app-types/make-root {::app-types/Root ContactsApp})}))
