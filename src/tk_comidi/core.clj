(ns tk-comidi.core
  (:require [puppetlabs.trapperkeeper.core :refer [defservice]]
            [clojure.tools.logging :as log]
            [puppetlabs.comidi :as comidi]))

(defn build-routes
  [path]
  (comidi/context path
    (comidi/routes
      (comidi/GET "/foo/environment" request "An environment"))))

(defservice api-service
  [[:WebroutingService add-ring-handler get-route]]
  (init [this context]
    (let [path (get-route this)
          allroutes (build-routes path)
          app (-> (comidi/routes->handler allroutes))]
      (add-ring-handler this app)) context)

  (stop [this context]
        (log/info "Shutting down echo-service") context))
