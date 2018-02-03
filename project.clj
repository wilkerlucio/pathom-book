(defproject com.wsscode/pathom-book "1.0.0"
  :description "Pathom book, source code for examples"
  :url "https://wilkerlucio.github.io/pathom-book/DevelopersGuide.html"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.671"]
                 [org.clojure/core.async "0.3.465"]
                 [com.wsscode/pathom "2.0.0-beta1"]
                 [spec-coerce "1.0.0-alpha5"]
                 [fulcrologic/fulcro "2.1.6"]
                 [fulcrologic/fulcro-inspect "2.0.0-alpha5"]
                 [binaryage/devtools "0.9.9"]
                 [thheller/shadow-cljs "2.0.150"]])
