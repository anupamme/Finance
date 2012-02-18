
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
   (slot certainty (default 100.0)))

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

  ; Rules for picking the best body

  (rule (if preferred-goal1 is shortterm and preferred-goal2 is shortterm and preferred-goal3 is shortterm)
        (then best-cap is large with certainty 90 and
        	  best-color is hybrid with certainty 80 and
        	  best-sweetness is debt with certainty 70))
        	  
    (rule (if preferred-goal1 is shortterm and preferred-goal2 is shortterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is hybrid and
        	  best-sweetness is debt))

    (rule (if preferred-goal1 is shortterm and preferred-goal2 is shortterm and preferred-goal3 is longterm)
        (then best-cap is small and
        	  best-color is hybrid and
        	  best-sweetness is debt))
        	  
  (rule (if preferred-goal1 is longterm and preferred-goal2 is midterm and preferred-goal3 is shortterm)
        (then best-cap is large and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is shortterm and preferred-goal2 is midterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is equity and
        	  best-sweetness is debt))
        	  
    (rule (if preferred-goal1 is shortterm and preferred-goal2 is longterm and preferred-goal3 is shortterm)
        (then best-cap is small and
        	  best-color is equity and
        	  best-sweetness is debt))

    (rule (if preferred-goal1 is shortterm and preferred-goal2 is longterm and preferred-goal3 is midterm)
        (then best-cap is small and
        	  best-color is equity and
        	  best-sweetness is debt))
        	  
  (rule (if preferred-goal1 is shortterm and preferred-goal2 is longterm and preferred-goal3 is longterm)
        (then best-cap is small and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is shortterm and preferred-goal3 is shortterm)
        (then best-cap is large and
        	  best-color is hybrid and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is shortterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is hybrid and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is shortterm and preferred-goal3 is largeterm)
        (then best-cap is small and
        	  best-color is hybrid and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is midterm and preferred-goal3 is shortterm)
        (then best-cap is large and
        	  best-color is equity and
        	  best-sweetness is growth))

	(rule (if preferred-goal1 is midterm and preferred-goal2 is midterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is midterm and preferred-goal3 is longterm)
        (then best-cap is small and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is longterm and preferred-goal3 is shortterm)
        (then best-cap is large and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is longterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is midterm and preferred-goal2 is longterm and preferred-goal3 is longterm)
        (then best-cap is small and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is longterm and preferred-goal2 is shortterm and preferred-goal3 is shortterm)
        (then best-cap is large and
        	  best-color is hybrid and
        	  best-sweetness is growth))
                   
  (rule (if preferred-goal1 is longterm and preferred-goal2 is shortterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is hybrid and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is longterm and preferred-goal2 is shorterm and preferred-goal3 is longterm)
        (then best-cap is small and
        	  best-color is hybrid and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is longterm and preferred-goal2 is midterm and preferred-goal3 is shortterm)
        (then best-cap is large and
        	  best-color is equity and
        	  best-sweetness is growth))
  
  (rule (if preferred-goal1 is longterm and preferred-goal2 is midterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is longterm and preferred-goal2 is midterm and preferred-goal3 is longterm)
        (then best-cap is small and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is longterm and preferred-goal2 is longterm and preferred-goal3 is shortterm)
        (then best-cap is large and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is longterm and preferred-goal2 is longterm and preferred-goal3 is midterm)
        (then best-cap is medium and
        	  best-color is equity and
        	  best-sweetness is growth))

  (rule (if preferred-goal1 is longterm and preferred-goal2 is longterm and preferred-goal3 is longterm)
        (then best-cap is small and
        	  best-color is equity and
        	  best-sweetness is growth))

(rule (if preferred-goal1 is unknown)
        (then best-cap is small and
                  best-color is equity and
                  best-sweetness is growth))

(rule (if preferred-goal2 is unknown)
        (then best-cap is small and
                  best-color is equity and
                  best-sweetness is growth))

(rule (if preferred-goal3 is unknown)
        (then best-cap is small and
                  best-color is equity and
                  best-sweetness is growth))
)
;;************************
;;* WINE SELECTION RULES *
;;************************

(defmodule WINES (import MAIN ?ALL)
                 (export deffunction get-wine-list))

(deffacts any-attributes
  (attribute (name best-color) (value any))
  (attribute (name best-cap) (value any))
  (attribute (name best-sweetness) (value any)))

(deftemplate WINES::wine
  (slot name (default ?NONE))
  (multislot color (default any))
  (multislot body (default any))
  (multislot sweetness (default any)))

(deffacts WINES::the-wine-list 
  (wine (name "DSP Black Rock - Top 100 equity") (color equity) (body medium) (sweetness debt))
  (wine (name "Franklin India BlueChip") (color hybrid) (body small) (sweetness growth))
  (wine (name "Birla Sunlife - Frontline Equity") (color hybrid) (body medium) (sweetness growth))
  (wine (name "Fidelity Equity") (color hybrid) (body medium large) (sweetness medium growth))
  (wine (name "UTI Dividend Yield") (color hybrid) (body small) (sweetness medium growth))
  (wine (name "AIG India Equity Regular") (color hybrid) (body small medium) (sweetness debt))
  (wine (name "Templeton India Growth") (color hybrid) (body large))
  (wine (name "DSP BlackRock - Equity Regular 2") (color hybrid) (body small) (sweetness debt))
  (wine (name "IDBI Nifty Junior Index 3") (color equity) (body small))
  (wine (name "DSP Blackrock - Small and Mid Cap") (color equity) (sweetness growth debt))
  (wine (name "HDFC Mid Cap") (color equity) (sweetness growth debt))
  (wine (name "DSP BlackRock MicroCap") (color equity) (body medium) (sweetness debt))
  (wine (name "Fidelity Tax Advantage") (color equity) (body large))
  (wine (name "AIG Infrastructure and Economic Reform") (color equity) (sweetness growth debt)))
  
  
(defrule WINES::generate-wines
  (wine (name ?name)
        (color $? ?c $?)
        (body $? ?b $?)
        (sweetness $? ?s $?))
  (attribute (name best-color) (value ?c) (certainty ?certainty-1))
  (attribute (name best-cap) (value ?b) (certainty ?certainty-2))
  (attribute (name best-sweetness) (value ?s) (certainty ?certainty-3))
  =>
  (assert (attribute (name wine) (value ?name)
                     (certainty (min ?certainty-1 ?certainty-2 ?certainty-3)))))

(defrule WINES::generate-wines-all
  (wine (name ?name)
        (color $? ?c $?)
        (body $? ?b $?)
        (sweetness $? ?s $?))
  =>
  (assert (attribute (name wine) (value ?name)
                     (certainty 100))))

(deffunction WINES::wine-sort (?w1 ?w2)
   (< (fact-slot-value ?w1 certainty)
      (fact-slot-value ?w2 certainty)))
      
(deffunction WINES::get-wine-list ()
  (bind ?facts (find-all-facts ((?f attribute))
                               (and (eq ?f:name wine)
                                    (>= ?f:certainty 20))))
  (sort wine-sort ?facts))
  

