(ns tk-comidi.core
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [puppetlabs.comidi :as comidi]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [schema.coerce :as coerce]
            [schema.core :as s]))

;;
;; Schemas
;;
(s/defschema Job
  {:id s/Int
   :environment String})

;;
;; Routes
;;
(defn build-hateoas-routes
  [path])

(def parse-job
  (coerce/coercer Job coerce/json-coercion-matcher))

(defn build-routes
  [path]
  (comidi/context path
    (comidi/routes
      (comidi/GET "/environment" request
                  {:status 200
                   :body "An environment"})
      (comidi/POST ["/testenv/" :testenv] [testenv]
                   (println (str "Testenv " testenv))
                   {:status 200
                    :body "Some text was returned"})
      (comidi/PUT ["/environment/test/" [#".*" :rest]] request
                  (let [body (slurp (:body request))
                        json-body (json/parse-string body (fn [k] (keyword k)))]
                    (println json-body)
                    (println (parse-job json-body)))

                  {:orig-req request
                   :rest (-> request :route-params :rest)}))))

(defservice api-service
  [[:WebroutingService add-ring-handler get-route]]
  (init [this context]
    (let [path (get-route this)
          allroutes (build-routes path)
          app (-> (comidi/routes->handler allroutes))]
      (add-ring-handler this app)) context)

  (stop [this context]
        (log/info "Shutting down echo-service") context))
