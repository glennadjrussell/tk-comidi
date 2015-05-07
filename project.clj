(defproject tk-comidi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [puppetlabs/trapperkeeper "1.0.0"]
                 [puppetlabs/trapperkeeper-webserver-jetty9 "1.3.1"]
                 [puppetlabs/kitchensink "1.1.0"]
                 [puppetlabs/comidi "0.1.1"]]

  :profiles {:dev {:source-paths ["dev"]}
             :dependencies [[puppetlabs/trapperkeeper "1.0.0"]
                            [puppetlabs/kitchensink "1.0.0"]]}

  :aliases {"tk" ["trampoline" "run" "--config" "dev-resources/config.conf"]}

  :main puppetlabs.trapperkeeper.main

  :target-path "target/%s")
;;  :profiles {:uberjar {:aot :all}})
