(ns com.wsscode.pathom.book.graphql.edn-converter
  (:require [fulcro.client.primitives :as fp]
            [fulcro.client.dom :as dom]
            [com.wsscode.pathom.book.app-types :as app-types]))

(fp/defsc EdnGraphqlConverter
  [this {::keys []} _ css]
  {:initial-state {::id (random-uuid)}
   :ident         [::id ::id]
   :query         [::id]
   :css           []
   :css-include   []}
  (dom/div nil
    "Editor goes here"))

(def contacts-app (fp/factory EdnGraphqlConverter))

(app-types/register-app "edn-graphql-converter"
  (fn []
    {::app-types/root (app-types/make-root {::app-types/Root EdnGraphqlConverter})}))
