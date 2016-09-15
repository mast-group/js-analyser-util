# js-analyser-util
##Util package to analyse instrumented and collected data from Node.JS projects using [js-analyser](https://github.com/mast-group/js-analyser)

###Usage
**mvn package** to build jar with dependencies

###Save summary from raw instrumented variable values
```
java -cp target/jsanalyser_util-1.0-SNAPSHOT-jar-with-dependencies.jar variable.summary.VariablesAndValues  <rawInstrumentLogfile> [OPTIONAL-summaryFilePath | DEFAULT- [rawInstrumentLogfile]summary.txt]
```

###Further analyse variable values using summary file created
```
java -cp target/jsanalyser_util-1.0-SNAPSHOT-jar-with-dependencies.jar variable.summary.VariableValuesIntoFiles 

Options:
  * -i, --input
       Input path of summary file
  * -o, --output
       Output path of result. Valid folder to save each variable in seperate
       file. Valid text file to save all variables in single file
    -q, --quartile
       Include to save quartiles of each variable in a single file rather than
       every value
       Default: false
    -s, --single
       Include to save all variables in singe file. Default is to save each
       variable in seperate file
       Default: false
    -t, --total
       Threshold to select variables with minimum total values
       Default: 50
    -u, --unique
       Threshold to select variables with minimum unique values
       Default: 1

```

####Save all values of all variables into one file as variable name -> value pair. (Default task)

Ex: `variable1.csv`

| variable1  | value |
|------| ---------: | 
|v1 | 20  | 
|v1 | 2   | 
|v2 | 232 |


####Save each variable into seperate file with all values recorded for that variable. (Include -s option to enable this)

Ex: `variable1.csv`

| variable1  | 
| ---------: | 
| 20  | 
| 2   | 
| 232 |


####Save each variable into seperate file with all values recorded for that variable. (Include -s option to enable this)

Ex: `variable1.csv`

``norm- Quartiles of normalized values (Values normalzied between 0-1)``

| Name|min|1st quartile|median|3rd quartile|max|norm 1st quartile|norm median|norm 3rd quartile|
|-----|--:|-----------:|-----:|-----------:|--:|----------------:|----------:|----------------:|
|var1 |-10.12|     -7.2|    -2|           3|11.23|           0.25|        0.4|              0.8| 
|var2 | 12|          71|   201|         312| 1121|            0.2|        0.6|              0.9| 


###Other util Classes

####github
This package contains classes to analyse GitHub usiing its APi.
- List repositiories based on search query
- List files based on the frequency of changes in given commits 
- List commit ids with certain additions/delitions

####cluster.em
- EM CLustering algorithm implementation using Mixture of Gaussians
- Gaussians per cluster is dynamic thus, automatically defined based on the cluster values

