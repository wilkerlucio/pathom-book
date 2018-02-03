(ns com.wsscode.pathom.book.graphql.edn-converter
  (:require
    [com.wsscode.pathom.book.app-types :as app-types]
    [com.wsscode.pathom.book.ui.codemirror :as codemirror]
    [com.wsscode.pathom.graphql :as gql]
    [cljs.reader :refer [read-string]]
    [fulcro.client.primitives :as fp]
    [fulcro.client.dom :as dom]
    [fulcro.client.mutations :as mutations]
    [goog.string :as gstr]))

(defn js-name [s]
  (gstr/toCamelCase (name s)))

(mutations/defmutation update-query [{:keys [ui/om-next-query]}]
  (action [{:keys [state ref]}]
    (let [gql (try
                (let [gql (-> om-next-query
                              read-string
                              (gql/query->graphql #::gql{:js-name js-name}))]
                  (swap! state assoc-in (conj ref :ui/translate-error?) false)
                  gql)
                (catch :default _
                  (swap! state assoc-in (conj ref :ui/translate-error?) true)
                  (get-in @state (conj ref :ui/graphql-query))))]
      (swap! state (comp #(assoc-in % (conj ref :ui/om-next-query) om-next-query)
                         #(assoc-in % (conj ref :ui/graphql-query) gql))))))

(fp/defsc OmGraphQlQueryTranslator
  [this {:ui/keys [om-next-query graphql-query translate-error?]} _ css]
  {:initial-state (fn [params]
                    (merge {::id                 (random-uuid)
                            :ui/om-next-query    "[]"
                            :ui/graphql-query    ""
                            :ui/translate-error? false}
                           params))
   :ident         [::id ::id]
   :query         [::id :ui/om-next-query :ui/graphql-query :ui/translate-error?]
   :css           [[:.container {:display       "grid" :grid-template-columns "50% 50%"
                                 :margin-bottom "20px"}
                    [:pre {:margin "0"}]
                    [:.CodeMirror {:height "100%"}]]
                   [:.translate-error {:color "#f00"}]]}
  (dom/div #js {:className (str (:container css) " " (if translate-error? (:translate-error css) ""))}
    (codemirror/clojure {:value    om-next-query
                         :onChange #(fp/transact! this `[(update-query {:ui/om-next-query ~%})])})
    (dom/pre #js {:className (if translate-error? (:translate-error css) "")}
      graphql-query)))

(app-types/register-app "edn-graphql-converter"
  (fn []
    {::app-types/root (app-types/make-root OmGraphQlQueryTranslator "graph-converter")}))
