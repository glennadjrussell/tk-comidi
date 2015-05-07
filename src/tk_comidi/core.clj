(ns tk-comidi.core
  (:require [puppetlabs.trapperkeeper.core :refer [defservice]]
            [clojure.tools.logging :as log]
            [puppetlabs.comidi :refer :all]))

(defservice api-service
  [[:WebroutingService add-ring-handler get-route]]
  (init [this context]
    (let [allroutes (context "/api"
                      (routes
                        (GET "/foo/environment" request "An environment")))
          app (-> (routes->handler allroutes))]
      (add-ring-handler this app)) context)

  (stop [this context]
        (log/info "Shutting down echo-service") context))
