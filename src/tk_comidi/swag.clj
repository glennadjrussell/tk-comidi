(ns puppetlabs.deployer.swag
  (:require [cheshire.core :as json]
            [puppetlabs.comidi :as comidi]
            [schema.coerce :as coerce]
            [schema.core :as schema]))

(defmacro route-it
  [path bindings]
  `(comidi/GET ~path ~bindings request {}))

;;
;; comidi-route
;;
(defn comidi-route [path route-map]
  (if (map? route-map)
    (doseq [[method method-def] route-map]
      (println method)
      (case method
;;        :get (comidi/GET path (route-parameters (:parameters method-def)) request {})
;;        :post (comidi/POST path (route-parameters (:parameters method-def)) request {})))))
        :get '(route-it path [])
        :post '(route-it path [])))))

;;
;;
;;
(defn comidi-app-from-swagger [app]
  (if (map? app)
    (let [paths (:paths app)]
      (doseq [[path route-config] paths]
        (comidi-route path route-config)))
    (println "ERROR: Invalid input to app generator")))

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
  (comidi/context ""
    (comidi-app-from-swagger root-uri-swagger)))
