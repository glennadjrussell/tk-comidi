(ns tk-comidi.core-test
  (:require [clojure.test :refer :all]
            [bidi.bidi :as bidi]
            [tk-comidi.core :refer :all]
            [puppetlabs.trapperkeeper.testutils.bootstrap :refer [with-app-with-config]]
            [puppetlabs.trapperkeeper.services.webrouting.webrouting-service :refer [webrouting-service]]
            [puppetlabs.trapperkeeper.services.webserver.jetty9-service :refer [jetty9-service]]
            [puppetlabs.http.client.sync :as http-client]))

(deftest test-routes
  (testing "routes should match"
    (let [allroutes (build-routes "/api")]
      (is (not (nil? (bidi/match-route allroutes "/api/foo/environment" :request-method :get)))))))

(deftest test-app
  (with-app-with-config app
    [jetty9-service
     webrouting-service
     api-service]
    {:webserver {:host "localhost" :port 8080}
     :web-router-service {:tk-comidi.core/api-service "/my-awesome-api"}}
    (let [resp (http-client/get "http://localhost:8080/my-awesome-api/foo/environment" {:as :text})]
      (is (= 200 (:status resp)))
      (is (= "An environment" (:body resp))))))
