(ns tk-comidi.core
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [puppetlabs.comidi :as comidi]
            [puppetlabs.trapperkeeper.core :refer [defservice]]
            [schema.coerce :as coerce]
            [schema.core :as s]))

;;
;; Macros
;;
;; Need to be able to render apis that look like the following:
;;
;; {
;;   "result": "someresult",
;;   "links": [
;;     {
;;       "name": "resultone",
;;       "href": "/results/resultone"
;;     }
;;   ]
;; }
;;(defn render-endpoint [props link-fn handler-fn])
;;(defn render-link [path handler-fn]) ;; this should be a macro that results in a comidi route
;;(defn render-resource [res-def])

;;(defmacro link-to [rel uri handler-fn]
;;  `(GET ~uri [] ~handler-fn))
;;  uri)

;;(defn link-to-resource [ep]
;;  (if (map? ep) (:uri ep)))

;;(defn link [rel method ep]
;;  (if (map? ep) (:uri ep)))

;;
;; Takes a map of the api, and builds the necessary route map
;;(defmacro defapp [api-map]
;;  `(if (map? api-map) (println "Hey")))

;;
;; Schemas
;;
(s/defschema Job
  {:id s/Int
   :environment String})

(def parse-job
  (coerce/coercer Job coerce/json-coercion-matcher))

;;(def application-routes
;;  (vector
;;    (comidi/GET "/" request
;;                {:status 200 :body "This is a result"})
;;    (comidi/GET ["/environments" :environment "/instances"] [environment])))

;;
;; Idea is to build mini apps
;;
;; What would this look like if it was returned?
;;
;;(defn instance-endpoint [id]
;;  {:self (str "/instance/" id)
;;   :stop (str "/instance/:id" (fn [x] x))})

;;(def application-endpoint
;;  {:uri "applications"
;;   :description "Returns a list of all applications"
;;   :routes application-routes
;;   :instances (link-to-resource instance-endpoint)})

;;(defn deploy-fn [])
;;(defn undeploy-fn [])

(defn route-parameters [parameters]
  (let [path-parameter (:path parameters)]
    (vec
      (doseq [[id param] path-parameter]
        id))))

(defmacro route-it
  [path bindings]
  `(comidi/GET ~path ~bindings request {}))

(defn maroute []
  (comidi/GET "/" request {}))

;;
;; comidi-route
;;
(defn comidi-route [path route-map]
  (if (map? route-map)
    (for [[method method-def] route-map]
      (case method
;;        :get (comidi/GET path (route-parameters (:parameters method-def)) request {})
;;        :post (comidi/POST path (route-parameters (:parameters method-def)) request {})))))
        :get '(comidi/GET path [] request {})
        :post '(comidi/GET path [] request {})))))
        ;;:get '(route-it path [])
        ;;:post '(route-it path [])))))

;;
;;
;;
(defn comidi-app-from-swagger [app]
  (if (map? app)
    (let [paths (:paths app)]
      (println paths)
      (comidi/routes
        (for [[path route-config] paths]
          (comidi-route path route-config))))
    (println "Not a map, derp")))

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

(defn build-routes
  [path]
  (comidi/context path
    (comidi/routes
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
      (add-ring-handler this app) (comidi-app root-uri-swagger)) context)
;;          (add-ring-handler this app) (build-routes "")) context)

  (stop [this context]
        (log/info "Shutting down echo-service") context))
