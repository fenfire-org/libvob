=============================================================
PEG ``vob_calendar--mudyc``: Calendar rendering
=============================================================

:Authors:  Matti Katila
:Stakeholders: Tuomas Lukka
:Last-Modified: $Date: 2003/09/17 13:09:25 $
:Revision: $Revision: 1.2 $
:Status:   Irrelevant


Overview
--------

Calendar is a renderable which can be used to generate an annual calendar.
This is one of the vital components in PP-module/project.


Basics
------

One calendar is one month. Usually only parts of it are shown. 
The whole month is rendered every time.


Issues
------

What is needed from framework to render Calendar?

- Coordinating system::

		           _______________ 
		           |___________|_| 
	----------         |_|_|_|_|_|_|_| 
	| Screen |   <---  |_|_|_|_|_|_|_| 
	|        |         |_|_|_|_|_|_|_| 
	----------         |_|_|_|_|_|_|_|
	                   |_|_|_________| 

- object => window

	- Text ratio, (Form/layout):

	- Text's ratio to month's calendar skeleton *must* be 
	  changeable from java side.

- Localization:

	- Months and Weekdays.

	- Solve in java side.

What is needed to make Calendar look good?

- Month (and year)

- Weeknumbers

- Weekdays

- Date(s)

- Colors

- LineWidth

- Text::

	------------------------------------------------------------------------------
	|                                                                            |
	|                             October 2002                                   |
	|                                                                            |
	|       Monday | Tuesday | Wednesday | Thursday | Friday | Saturday | Sunday |
	+-----+--------+---------+-----------+----------+--------+----------+--------+
	|x    |        |         |           |          |        |          |        |
	|     |        |         |           |          |        |          |        |
	|     |        |    1    |     2     |     3    |   4    |    5     |   6    |
	|     |        |         |           |          |        |          |        |
	+-----+--------+---------+-----------+----------+--------+----------+--------+
	|x+1  |        |         |           |          |        |          |        |
	|     |        |         |           |          |        |          |        |
	|     |   7    |    8    |     9     |    10    |   11   |    12    |   13   |
	|     |        |         |           |          |        |          |        |
	+-----+--------+---------+-----------+----------+--------+----------+--------+
	|x+2  |        |         |           |          |        |          |        |
	|     |        |         |           |          |        |          |        |
	|     |  etc.. |         |           |          |        |          |        |


	|<-w->|<--d1-->|
   
- w's ratio to d1::
        
	---------------------------   ---
	|                             /|\
	|                              m
	|                              |
	|       Monday | Tuesday |    \|/
	+-----+--------+---------+-   ---
	|x    |        |         |    /|\
	|     |        |         |     d2
	|     | empty  |    1    |     |
	|     |        |         |    \|/
	+-----+--------+---------+-  ----

- m's ratio to d2

	- must know how many empty days,
        
	- must know how many days and
        
	- must know how many weeks in month::

		     April             December
		______________     _______________   
		|_|_|_|_|_|_|_|    |___________|_|  
		|_|_|_|_|_|_|_|    |_|_|_|_|_|_|_|
		|_|_|_|_|_|_|_|    |_|_|_|_|_|_|_|
		|_|_|_|_|_|_|_|    |_|_|_|_|_|_|_|
		. here is     .    |_|_|_|_|_|_|_|
		....emptynes...    |_|_|_________|

		_______________  
		|___________|_|  /|\
		|_|_|_|_|_|_|_|   |
		|_|_|_|_|_|_|_| -height (4...6)
		|_|_|_|_|_|_|_|   |
		|_|_|_|_|_|_|_|   |
		|_|_|_________|  \|/

Why CalendarVob isn't made with CallGL?

    - Text is very problematic - can't be done.
    

