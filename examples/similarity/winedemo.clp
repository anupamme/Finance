
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
   (slot performance)
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
              (then ?attribute is ?value  ?c2 $?rest))
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
; Rules for picking the best fundType

	(rule (if preferred-fundType is equity)
		(then best-fundType is equity  80 and
		      best-fundType is hybrid  20))
		
	(rule (if preferred-fundType is hybrid)
		(then best-fundType is equity  20 and
		      best-fundType is hybrid  80))
		
	(rule (if preferred-fundType is unknown)
		(then best-fundType is equity  40 and
		      best-fundType is hybrid  40))

; Rules for picking the best sector

	(rule (if preferred-sector is material)
		(then best-sector is material ))
		
	(rule (if preferred-sector is capitalGoods)
		(then best-sector is capitalGoods ))
		
	(rule (if preferred-sector is conglomerates)
		(then best-sector is conglomerates ))
		
	(rule (if preferred-sector is construction)
		(then best-sector is construction ))
		
	(rule (if preferred-sector is energy)
		(then best-sector is energy ))
		
	(rule (if preferred-sector is financial)
		(then best-sector is financial ))
		
	(rule (if preferred-sector is healthcare)
		(then best-sector is healthcare ))
		
	(rule (if preferred-sector is utilities)
		(then best-sector is utilities ))
		
	(rule (if preferred-sector is transportation)
		(then best-sector is transportation ))
		
	(rule (if preferred-sector is technology)
		(then best-sector is technology ))
		
  	(rule (if preferred-sector is services)
        	(then best-sector is services ))
)

;;************************
;;* WINE SELECTION RULES *
;;************************

(defmodule WINES (import MAIN ?ALL)
                 (export deffunction get-wine-list))

(deffacts any-attributes
  (attribute (name best-fundType) (value any))
;  (attribute (name best-fundCap) (value any))
;  (attribute (name best-fundPurpose) (value any))
  (attribute (name best-sector) (value any)))

(deftemplate WINES::wine
  (slot name (default ?NONE))
  (multislot fundType (default any))
  (multislot fundCap (default any))
  (multislot fundPurpose (default any))
  (slot performance (default any))
  (multislot sector (default any)))

(deffacts WINES::the-wine-list 
  (wine (name "DSP Black Rock - Top 100 equity") (fundType equity) (fundCap large) (fundPurpose growth) (sector technology healthcare) (performance 12))
  (wine (name "Franklin India BlueChip") (fundType equity) (fundCap large) (fundPurpose growth) (sector financial technology services) (performance 14))
  (wine (name "Franklin India Index NSE Nifty") (fundType equity) (fundCap large) (fundPurpose growth) (sector financial) (performance 11))
  (wine (name "Goldman Sachs Nifty ETS 1") (fundType equity) (fundCap large) (fundPurpose growth) (sector capitalGoods financial) (performance 12))
  (wine (name "ICICI Prudential - Focussed BlueChip Equity") (fundType equity) (fundCap large) (fundPurpose growth) (sector conglomerates))
  (wine (name "Kotak - Sensex ETF 1") (fundType equity) (fundCap large) (fundPurpose growth) (sector construction))
;; mid cap funds.  
(wine (name "Birla Sunlife - Frontline Equity") (fundType equity) (fundCap medium) (fundPurpose growth) (sector energy))
  (wine (name "Fidelity Equity") (fundType equity) (fundCap medium large) (fundPurpose medium growth) (sector technology energy))
  (wine (name "Franklin India Prima Plus") (fundType equity) (fundCap medium large) (fundPurpose medium growth) (sector financial technology))
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
        (fundType $?)
        (fundCap $?)
        (fundPurpose $?)
	(performance ?p)
	(sector $? ?sec $?))
  (attribute (name best-sector) (value ?sec) (certainty ?certainty-1))
  (attribute (name best-fundType) (value ?ftype) (certainty ?certainty-2))
  =>
  (assert (attribute (name wine) (value ?name) (sector ?sec) (performance ?p))))
;                     (certainty (min ?certainty-1 ?certainty-2)))))
;                      (certainty ?certainty-1))))

(deffunction WINES::wine-sort (?w1 ?w2)
   (< (fact-slot-value ?w1 certainty)
      (fact-slot-value ?w2 certainty)))
      
(deffunction WINES::get-wine-list ()
  (bind ?facts (find-all-facts ((?f attribute))
                               (and (eq ?f:name wine)
                                    (>= ?f:certainty 20))))
  (sort wine-sort ?facts))
