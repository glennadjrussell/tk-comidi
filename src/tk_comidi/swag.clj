(ns puppetlabs.deployer.swag
  (:require [cheshire.core :as json]
            [puppetlabs.comidi :as comidi]
            [schema.coerce :as coerce]
            [schema.core :as schema]))

;;
;; Note to self:
;; If we are truly HATEOAS, then uri generation should be incidental. The important element are the link relations
;;
(def env-record
  {:name "env"
   :link app-record})

(def inst-record
  {:name "instance"})

(def app-record
  {:name "app"
   :kind "application"
   :link inst-record
   })

(schema/defschema links
  {:rel s/Str
   :method (schema/enum :get :post)
   :href s/Str})

(schema/defschema settings
  {:modulepath [s/Str]
   :manifest s/Str
   :environment_timeout s/Int
   :config_version s/Str})

(schema/defschema environment
  {:settings settings
   })

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
