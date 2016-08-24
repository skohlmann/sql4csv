# Table of content

* [What is sql4csv?](#what_is_sql4csv)
* [Why your own solution and not using an existing ones?](#why_own_solution)
* [Download and Installation](#installation)
* [Caveats](#caveats)
    * [`from` keyword](#from_keyword)



# <a name='what_is_sql4csv' />What is sql4csv?

`sql4csv` is a simple tool to execute SQL querys direct from commandline
on the data of a [CSV](https://en.wikipedia.org/wiki/Comma-separated_values "CSV at Wikipedia")
file.

To execute a SQL statement on a CSV file simple execute the following command:

        scq select distinct author from books.csv

were `scq` is the name of the command to execute.

In case of a CSV file looking like the following


        author,title  
        Douglas Adams,Doctor Who  
        Douglas Adams,The Hitchhiker's Guide to the Galaxy  
        Terry Pratchet,The Colour of Magic  
        Terry Pratchet,Mort  


the result, printed in the console will be 

        author  
        Douglas Adams  
        Terry Pratchet


# <a name='why_own_solution' />Why your own solution and not using an existing ones?

Because I can :-)

There are a bunch of tools out there to do such a solution. The most impressive is
[q](https://github.com/harelba/q "q - Text as Data"). Nevertheless `scq`
is written by me to have a project using the new features of Java 8 from scratch.

Also I want to do some automatic type detection for lazy types like dates and
datetimes. Reason are the CSV files I sometimes have where dates are written in
an ISO like format (2016-8-3 instead of 2016-08-03).

But why do I need such a tool?

It's for my simple statistic stack based on Unix command line tools,
[R](https://www.r-project.org/ "R Project Homepage") and some simple SQL scripts
which I don't want to import to a dedicated RDBMS for only onetime usage.

# <a name='installation' />Download and Installation

1. Download the latest prebuild version from our homepage: 
   [sql4csv-0.1.0-bin.zip](http://www.speexx.de/sql4csv/sql4csv-0.1.0-bin.zip).
2. Unpack the archive with the tool of your choice.
3. Adjust your `PATH` environment variable by adding the `sql4csv` directory.

That's it. Have fun :-)

# <a name='caveats' />Caveats

Using SQL statements direct from a `bash` command line is problematic.
The `bash` replaces the asterisk (`*`) sign with the files of the current
directory. It's not simple possible to escape this behavior. I work around this
with replaceing the <tt>*</tt> character with 

> <tt>$(head -n 1 _filename_)</tt>

Not a very elegant method but it works. 

Escaping additional special `bash` characters like parenthesis or less-
and greater-than equal signs is straight forward with a leading basckslash
(`\`).

## <a name='from_keyword' />`from` keyword

`scq` supports space characters in table columns. Nevertheless if a table
column contains a space surrounded `from`, the beahvior is not supported yet.

