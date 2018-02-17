(defproject com.wsscode/pathom-book "1.0.0"
  :description "Pathom book, source code for examples"
  :url "https://wilkerlucio.github.io/pathom-book/DevelopersGuide.html"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"]
                 [com.wsscode/pathom "2.0.0-beta2-SNAPSHOT"]
                 [spec-coerce "1.0.0-alpha5"]
                 [fulcrologic/fulcro "2.3.0-SNAPSHOT"]
                 [fulcrologic/fulcro-inspect "2.0.0-alpha6-SNAPSHOT"]
                 [binaryage/devtools "0.9.9"]
                 [thheller/shadow-cljs "2.1.14"]])
