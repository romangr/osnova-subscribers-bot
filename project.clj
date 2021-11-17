(defproject subscribers-bot "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [morse "0.4.3"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.xerial/sqlite-jdbc "3.36.0.3"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [commons-lang/commons-lang "2.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.32"]]
  :resource-paths ["src/resources"]
  :java-source-paths ["src/java"]
  :source-paths ["src/clojure"]
  :repl-options {:init-ns subscribers-bot.core})
