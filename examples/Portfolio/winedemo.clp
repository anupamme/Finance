
;;;======================================================
;;;   Wine Expert Sample Problem
;;;
;;;     WINEX: The WINe EXpert system.
;;;     This example selects an appropriate wine
;;;     to drink with a meal.
;;;
;;;     CLIPS Version 6.3 Example
;;;
;;;     For use with the CLIPSJNI
;;;======================================================

(defmodule MAIN (export ?ALL))

;;*****************
;;* INITIAL STATE *
;;*****************

(deftemplate MAIN::attribute
   (slot name)
   (slot value)
   (slot certainty (default 100.0))
   (multislot sector))

(defrule MAIN::start
  (declare (salience 10000))
  =>
  (set-fact-duplication TRUE)
  (focus CHOOSE-QUALITIES WINES))

(defrule MAIN::combine-certainties ""
  (declare (salience 100)
           (auto-focus TRUE))
  ?rem1 <- (attribute (name ?rel) (value ?val) (certainty ?per1))
  ?rem2 <- (attribute (name ?rel) (value ?val) (certainty ?per2))
  (test (neq ?rem1 ?rem2))
  =>
  (retract ?rem1)
  (modify ?rem2 (certainty (/ (- (* 100 (+ ?per1 ?per2)) (* ?per1 ?per2)) 100))))
  
 
;;******************
;; The RULES module
;;******************

(defmodule RULES (import MAIN ?ALL) (export ?ALL))

(deftemplate RULES::rule
  (slot certainty (default 100.0))
  (multislot if)
  (multislot then))

(defrule RULES::throw-away-ands-in-antecedent
  ?f <- (rule (if and $?rest))
  =>
  (modify ?f (if ?rest)))

(defrule RULES::throw-away-ands-in-consequent
  ?f <- (rule (then and $?rest))
  =>
  (modify ?f (then ?rest)))

(defrule RULES::remove-is-condition-when-satisfied
  ?f <- (rule (certainty ?c1) 
              (if ?attribute is ?value $?rest))
  (attribute (name ?attribute) 
             (value ?value) 
             (certainty ?c2))
  =>
  (modify ?f (certainty (min ?c1 ?c2)) (if ?rest)))

(defrule RULES::remove-is-not-condition-when-satisfied
  ?f <- (rule (certainty ?c1) 
              (if ?attribute is-not ?value $?rest))
  (attribute (name ?attribute) (value ~?value) (certainty ?c2))
  =>
  (modify ?f (certainty (min ?c1 ?c2)) (if ?rest)))

(defrule RULES::perform-rule-consequent-with-certainty
  ?f <- (rule (certainty ?c1) 
              (if) 
              (then ?attribute is ?value with certainty ?c2 $?rest))
  =>
  (modify ?f (then ?rest))
  (assert (attribute (name ?attribute) 
                     (value ?value)
                     (certainty (/ (* ?c1 ?c2) 100)))))

(defrule RULES::perform-rule-consequent-without-certainty
  ?f <- (rule (certainty ?c1)
              (if)
              (then ?attribute is ?value $?rest))
  (test (or (eq (length$ ?rest) 0)
            (neq (nth 1 ?rest) with)))
  =>
  (modify ?f (then ?rest))
  (assert (attribute (name ?attribute) (value ?value) (certainty ?c1))))

;;*******************************
;;* CHOOSE WINE QUALITIES RULES *
;;*******************************

(defmodule CHOOSE-QUALITIES (import RULES ?ALL)
                            (import MAIN ?ALL))

(defrule CHOOSE-QUALITIES::startit => (focus RULES))

(deffacts the-wine-rules

  ; Rules for picking the best fundCap

  (rule (if has-riskappetite is yes and 
            riskappetite is moderate)
        (then best-fundCap is large))

  (rule (if horizon is short)
        (then best-fundCap is large))

  (rule (if horizon is mid)
        (then best-fundCap is small with certainty 30 and
              best-fundCap is medium with certainty 60 and
              best-fundCap is large with certainty 30))

  (rule (if horizon is long)
        (then 
	      best-fundCap is small with certainty 60
	      best-fundCap is medium with certainty 30 and
              best-fundCap is large with certainty 10))

  (rule (if has-riskappetite is yes and
            riskappetite is aggressive)
        (then best-fundCap is small with certainty 40 and
              best-fundCap is medium with certainty 60))

  (rule (if preferred-cap is large)
        (then best-fundCap is large with certainty 40))

  (rule (if preferred-cap is medium)
        (then best-fundCap is medium with certainty 40))

  (rule (if preferred-cap is small) 
        (then best-fundCap is small with certainty 40))

  (rule (if preferred-cap is small and
            best-fundCap is large)
        (then best-fundCap is medium))

  (rule (if preferred-cap is large and
            best-fundCap is small)
        (then best-fundCap is medium))

  (rule (if preferred-cap is unknown) 
        (then best-fundCap is small with certainty 20 and
              best-fundCap is medium with certainty 20 and
              best-fundCap is large with certainty 20))

  ; Rules for picking the best fundType

  (rule (if main-goal is goallongterm)
        (then best-fundType is equity with certainty 90))

  (rule (if main-goal is goalmidterm and
            has-emergency is no)
        (then best-fundType is hybrid with certainty 90 and
              best-fundType is equity with certainty 10))

  (rule (if main-goal is goalmidterm and
            has-emergency is yes)
        (then best-fundType is equity with certainty 20 and
              best-fundType is hybrid with certainty 80))

  (rule (if main-goal is goalshortterm)
        (then best-fundType is hybrid))

  (rule (if main-goal is-not goalshortterm and
            has-riskappetite is yes and
            riskappetite is blind)
        (then best-fundType is equity))

  (rule (if has-riskappetite is yes and
            riskappetite is aggressive)
        (then best-fundType is hybrid with certainty 40))
                   
  (rule (if preferred-color is equity)
        (then best-fundType is equity with certainty 40))

  (rule (if preferred-color is hybrid)
        (then best-fundType is hybrid with certainty 40))

  (rule (if preferred-color is unknown)
        (then best-fundType is equity with certainty 20 and
              best-fundType is hybrid with certainty 20))
  
  ; Rules for picking the best fundPurpose

  (rule (if has-riskappetite is yes and
            riskappetite is moderate)
        (then best-fundPurpose is growth with certainty 90 and
              best-fundPurpose is debt with certainty 40))

  (rule (if preferred-purpose is taxplanning)
        (then best-fundPurpose is taxplanning with certainty 40))

  (rule (if preferred-purpose is debt)
        (then best-fundPurpose is debt with certainty 40))

  (rule (if preferred-purpose is growth)
        (then best-fundPurpose is growth with certainty 40))

  (rule (if best-fundPurpose is growth and
            preferred-purpose is taxplanning)
        (then best-fundPurpose is debt))

  (rule (if best-fundPurpose is taxplanning and
            preferred-purpose is growth) 
        (then best-fundPurpose is debt))

  (rule (if preferred-purpose is unknown)
        (then best-fundPurpose is taxplanning with certainty 20 and
              best-fundPurpose is debt with certainty 20 and
              best-fundPurpose is growth with certainty 20))

)

;;************************
;;* WINE SELECTION RULES *
;;************************

(defmodule WINES (import MAIN ?ALL)
                 (export deffunction get-wine-list))

(deffacts any-attributes
  (attribute (name best-fundType) (value any))
  (attribute (name best-fundCap) (value any))
  (attribute (name best-fundPurpose) (value any)))

(deftemplate WINES::wine
  (slot name (default ?NONE))
  (multislot fundType (default any))
  (multislot fundCap (default any))
  (multislot fundPurpose (default any))
  (slot performance (default any))
  (multislot sector (default any)))

(deffacts WINES::the-wine-list 
  (wine (name "DSP Black Rock - Top 100 equity") (fundType equity) (fundCap large) (fundPurpose growth) (sector energy financial))
  (wine (name "Franklin India BlueChip") (fundType equity) (fundCap large) (fundPurpose growth) (sector services))
  (wine (name "Franklin India Index NSE Nifty") (fundType equity) (fundCap large) (fundPurpose growth) (sector material ))
  (wine (name "Goldman Sachs Nifty ETS 1") (fundType equity) (fundCap large) (fundPurpose growth) (sector capitalGoods))
  (wine (name "ICICI Prudential - Focussed BlueChip Equity") (fundType equity) (fundCap large) (fundPurpose growth) (sector conglomerates))
  (wine (name "Kotak - Sensex ETF 1") (fundType equity) (fundCap large) (fundPurpose growth) (sector construction))
;; mid cap funds.  
(wine (name "Birla Sunlife - Frontline Equity") (fundType equity) (fundCap medium) (fundPurpose growth) (sector energy))
  (wine (name "Fidelity Equity") (fundType equity) (fundCap medium large) (fundPurpose medium growth) (sector technology energy))
  (wine (name "Franklin India Prima Plus") (fundType equity) (fundCap medium large) (fundPurpose medium growth) (sector technology energy))
  (wine (name "HDFC - Top 200") (fundType equity) (fundCap medium large) (fundPurpose medium growth))
  (wine (name "Mirae Asset - India Opportunities") (fundType equity) (fundCap medium large) (fundPurpose medium growth))
  (wine (name "ICICI Prudential - Dynamic") (fundType equity) (fundCap small) (fundPurpose medium taxplanning))
  (wine (name "UTI - Equity Div 2") (fundType equity) (fundCap small medium) (fundPurpose debt))
  (wine (name "Templeton India Growth") (fundType hybrid) (fundCap large))
  (wine (name "DSP BlackRock - Equity Regular 2") (fundType hybrid) (fundCap small) (fundPurpose debt))
  (wine (name "IDBI Nifty Junior Index 3") (fundType equity) (fundCap small))
  (wine (name "DSP Blackrock - Small and Mid Cap") (fundType equity) (fundPurpose taxplanning debt))
  (wine (name "HDFC Mid Cap") (fundType equity) (fundPurpose taxplanning debt))
  (wine (name "DSP BlackRock MicroCap") (fundType equity) (fundCap medium) (fundPurpose debt))
  (wine (name "Fidelity Tax Advantage") (fundType equity) (fundCap large))
  (wine (name "AIG Infrastructure and Economic Reform") (fundType equity) (fundPurpose taxplanning debt)))
  
  
(defrule WINES::generate-wines
  (wine (name ?name)
        (fundType $? ?c $?)
        (fundCap $? ?b $?)
        (fundPurpose $? ?s $?)
	(performance $?)
	(sector $?sec))
  (attribute (name best-fundType) (value ?c) (certainty ?certainty-1))
  (attribute (name best-fundCap) (value ?b) (certainty ?certainty-2))
  (attribute (name best-fundPurpose) (value ?s) (certainty ?certainty-3))
  =>
  (assert (attribute (name wine) (value ?name) (sector $?sec)
                     (certainty (min ?certainty-1 ?certainty-2 ?certainty-3)))))

(deffunction WINES::wine-sort (?w1 ?w2)
   (< (fact-slot-value ?w1 certainty)
      (fact-slot-value ?w2 certainty)))
      
(deffunction WINES::get-wine-list ()
  (bind ?facts (find-all-facts ((?f attribute))
                               (and (eq ?f:name wine)
                                    (>= ?f:certainty 20))))
  (sort wine-sort ?facts))
