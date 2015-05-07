(def tk-version "1.1.1")
(def tk-jetty9-version "1.3.1")
(def ks-version "1.1.0")

(defproject tk-comidi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :pedantic? :abort

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [puppetlabs/trapperkeeper ~tk-version
                  :exclusions [clj-time org.clojure/tools.macro]]
                 [puppetlabs/trapperkeeper-webserver-jetty9 ~tk-jetty9-version :exclusions [clj-time]]
                 [puppetlabs/kitchensink ~ks-version]
                 [puppetlabs/comidi "0.1.2" :exclusions [clj-time]]]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [puppetlabs/trapperkeeper ~tk-version
                                   :classifier "test"
                                   :exclusions [clj-time org.clojure/tools.macro]]
                                  [puppetlabs/kitchensink ~ks-version :classifier "test"]
                                  [puppetlabs/http-client "0.4.4" :exclusions [commons-io]]]}}

  :repl-options {:init-ns user}

  :aliases {"tk" ["trampoline" "run" "--config" "dev-resources/config.conf"]}

  :main puppetlabs.trapperkeeper.main

  :target-path "target/%s")
;;  :profiles {:uberjar {:aot :all}})
