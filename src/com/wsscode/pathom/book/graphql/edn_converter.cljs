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

(defn pretty-print-string [x]
  (with-out-str (cljs.pprint/pprint x)))

(mutations/defmutation update-query [{:keys [ui/om-next-query]}]
  (action [{:keys [state ref]}]
    (let [gql (try
                (let [gql (-> om-next-query
                              read-string
                              (gql/query->graphql {::gql/js-name js-name}))]
                  (swap! state assoc-in (conj ref :ui/translate-error?) false)
                  gql)
                (catch :default _
                  (swap! state assoc-in (conj ref :ui/translate-error?) true)
                  (get-in @state (conj ref :ui/graphql-query))))]
      (swap! state (comp #(assoc-in % (conj ref :ui/om-next-query) om-next-query)
                         #(assoc-in % (conj ref :ui/graphql-query) gql)))))
  (refresh [_] [:ui/om-next-query]))

(fp/defsc GraphQlQueryTranslator
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
    (codemirror/graphql {:value               graphql-query
                         ::codemirror/options {::codemirror/readOnly true}})))

(def graphql-query-translator (fp/factory GraphQlQueryTranslator))

(defn ref-transact! [this props ident-attr tx]
  (fp/transact! (fp/get-reconciler this) [ident-attr (get props ident-attr)] tx))

(fp/defsc QueryTranslatorWithDemos
  [this {::keys [translator query-examples]} _ css]
  {:initial-state (fn [params]
                    {::id             (random-uuid)
                     ::query-examples '{"Basic Query"        [:user/id :user/name]
                                        "Joins"              [{:app/me [:user/id :user/name]}]
                                        "Ident Query"        [{[:user/by-login "wilkerlucio"] [:bio :url]}
                                                              {[:organization/by-login "clojure"] [:name :url]}]
                                        "Parameterized join" [({:app/all-users [:user/id :user/name]}
                                                                {:limit 10})]
                                        "Union Query"        [{:app/timeline
                                                               {:app/User     [:user/id :user/name]
                                                                :app/Activity [:activity/id :activity/title
                                                                               {:activity/user
                                                                                [:user/name]}]}}]
                                        "Mutation"           [(users/create {:user/id 123 :user/name "Foo"})]}
                     ::translator     (fp/get-initial-state GraphQlQueryTranslator params)})
   :ident         [::id ::id]
   :query         [::id ::query-examples
                   {::translator (fp/get-query GraphQlQueryTranslator)}]
   :css           [[:.button {:border        "1px solid #ccc"
                              :border-radius "4px"
                              :color         "#333"
                              :outline       "0"
                              :margin-right  "10px"
                              :margin-bottom "12px"
                              :padding       "6px 12px"}
                    [:&:hover {:background-color "#e6e6e6"
                               :border-color     "#adadad"}]]]
   :css-include   [GraphQlQueryTranslator]}
  (dom/div nil
    (dom/div nil
      (for [[title example] query-examples]
        (dom/button #js {:key       title
                         :className (:button css)
                         :onClick   #(ref-transact! this translator ::id
                                       `[(update-query {:ui/om-next-query ~(pretty-print-string example)})])}
          title)))
    (graphql-query-translator translator)))

(app-types/register-app "edn-graphql-converter"
  (fn []
    {::app-types/root (app-types/make-root QueryTranslatorWithDemos "graph-converter")}))
