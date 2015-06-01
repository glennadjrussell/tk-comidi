(ns tk-comidi.core
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [puppetlabs.comidi :as comidi]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [schema.coerce :as coerce]
            [schema.core :as s]
            [slingshot.slingshot :as sling]))

;;
;; Schemas
;;
(s/defschema Job
  {:id s/Int
   :environment String})

(def parse-job
  (coerce/coercer Job coerce/json-coercion-matcher))

(defn generate-comidi-routes [app]
  (if (map? app)
    (let [paths (:paths app)]
      (comidi/context "/api"
        (for [[path route-config] paths]
          (for [[method method-def] route-config]
            (case method
              :get `(comidi/GET ~path request {})
              :post `(comidi/GET ~path request {}))))))
    (sling/throw+ {:message "Parameter isn't a map"})))

(defn comidi-app-from-swagger [app]
    (let [paths (:paths app)]
      (comidi/context "/api"
          (comidi/routes (generate-comidi-routes app)))))

;;
;; Site-map
;;
;; Follows swagger layout
;;
(def root-uri-swagger
  {:info
   {:title "Application Management API",
    :description "Multi-purpose API for performing application management operations"
    :version "1.0"}
   :paths {"/applications/:id/instances" {:get {
                                                :parameters {
                                                             :path {:id String}}}}
           "/deploy/:environment" {:post {}}}})

(defn comidi-app [json]
  (comidi/context "/test"
    (comidi-app-from-swagger root-uri-swagger)))

;;
;; Site-map
;;
;;(def root-uri
;;  {:info
;;   {:title "Application Management API",
;;    :description "Multi-purpose API for performing application management operations"
;;    :version "1.0"}
;;   :paths
;;   [{:link (link-to "application" "/applications" deploy-fn)}
;;    {:link (link-to "events" "/events" undeploy-fn)}
;;    {:link (link-to-resource application-endpoint)}]})

;;(def application-uri
;;  {:path "applications"
;;   :deploy (render-link "/application/deploy" deploy-fn)
;;   :undeploy (render-link "/application/undeploy/" undeploy-fn)})

;;
;; Root endpoint
;;
;;(def root-endpoint
;;  {:info
;;   {:version "1.0",
;;    :title "tk-comidi",
;;    :description "An example application using trapperkeeper and comidi"}
;;   :links application-endpoint})

(defn do-me-do-me [] (println "Deploy function"))

(def deploy-cmd
  {:request-params [{:app s/Str}]
   :body-params [{:tgt s/Str}]
   :return Job
   })

(defmacro defcommand [path & body]
  `(comidi/GET ~path request ~body))

(defn build-routes
  [path]
  (comidi/context path
    (comidi/routes
;;      (defcommand "/command/deploy" {:status 200 :body "Test"})
      (comidi/GET "/gen" request
                  {:status 200 :body (json/generate-string root-uri-swagger)})
      (comidi/GET ["/environment/" :envid "/interfaces"] [envid]
                  {:status 200
                   :body (format "An environment %s" envid)})
      (comidi/GET "/environment" request
                  {:status 200
                   :body "An environment"})
      (comidi/POST ["/testenv/" :testenv] request
                   (println (format "testenv : %s" (:testenv (:params request))))
                   {:status 200
                    :body "Some text was returned"})
      (comidi/PUT ["/environment/test/" [#".*" :rest]] request
                  (println (str "Regex matched " (:params request)))
                  (let [body (slurp (:body request))
                        json-body (json/parse-string body (fn [k] (keyword k)))]
                  {:orig-req request
                   :rest (-> request :route-params :rest)})))))

(defservice api-service
  [[:WebroutingService add-ring-handler get-route]]
  (init [this context]
    (let [path (get-route this)
          allroutes (build-routes path)
          ;;app (comidi/routes->handler allroutes)]
          app (comidi-app-from-swagger root-uri-swagger)]
      (add-ring-handler this app)) context)

  (stop [this context]
        (log/info "Shutting down echo-service") context))
