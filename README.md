# tpinfo-frontend

tpinfo-frontend consists of two web application showing information about integration and number of calls passing through [RIV-TA](https://rivta.se/documents.html#003) based service platforms. 
The applications are:  

**hippo** - https://integrationer.tjansteplattform.se/  
**statistik** - https://statistik.tjansteplattform.se/

(The backend reside in its own repository, see https://github.com/rojeras/tpinfo-backend)  

The Applications in tpinfo-frontend are defined as separate views in a shared code base.  

## Building blocks
tpinfo-frontend is implemented in [Kotlin](https://kotlinlang.org/). Kotlin is a modern, functional, strongly typed language. Kotlin is used on many platforms, not the least Android where Google is promoting it. The language can also be used for web development, Kotlin/JS. It was selected for tpinfo as a better alternative than Javascript.  
[KVision](https://kvision.io/) was further selected as the base framework. It contains a large number of ready to use GUI components and interfaces to known Javascript libraries. In tpinfo the following are used:  
* [Bootstrap](https://getbootstrap.com/) - GUI components
* [Redux](https://redux.js.org/) (Javascript Redux, not ReduxKotlin) - as a base for the reactive GUIs
* [Navigo](https://github.com/krasimir/navigo) - Browser navigation and history
* [Tabulator](http://tabulator.info/) - interactive tables in Statistik
* [Chart.js](https://www.chartjs.org/) - the charts in Statistik
* [Docker](https://www.docker.com/) - as execution environment

## Development setup

## Docker Build process

## KDoc documentation
