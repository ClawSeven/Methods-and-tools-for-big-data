# Lab 2

### awk

An AWK program is a series of pattern action pairs, written as:

```
condition { action }
condition { action }
```

The program tests each record against each of the conditions in turn, and executes the *action* for each expression that is true. Either the *condition* or the *action* may be omitted. **The *condition* defaults to matching every record. The default *action* is to print the record.** This is the same pattern-action structure as sed.

> If there is **NO** condition, the action will executed for every record.
>
> `BEGIN` means done before all records, and `END` means done after all records.

The *condition* can be `BEGIN` or `END` causing the *action* to be executed before or after all records have been read, or *pattern1, pattern2* which matches the range of records starting with a record that matches *pattern1* up to and including the record that matches *pattern2* before again trying to match against *pattern1* on future lines.

`$0` is the entire line, and `$1`, `$2`, … are the $k^{th}$ field variables.

**NR:** NR command keeps a current count of the number of input records.

**NF:** NF command keeps a count of the number of fields (eg. words) within the current input record.

**FS:** FS command contains the field separator character which is used to divide fields on the input line. The default is “white space”, meaning space and tab characters. FS can be reassigned to another character (typically in BEGIN) to change the field separator.



#### change field separator

```bash
$ awk -F "," '{print $1}' FILE 		# Use ',' as a field separator and print the first field
$ awk -F ":" '{print $2}' FILE		# Use ':' as a field separator and print the second field
```



#### find pattern

```sh
$ awk '/FIND_PATTERN/ { print $0 }' FILE
```



#### get program from file

```sh
$ awk -f SOURCE
```



#### UNDERSTANDING ‘#!’

`awk` is an *interpreted* language. This means that the `awk` utility reads your program and then processes your data according to the instructions in your program. (This is different from a *compiled* language such as C, where your program is first compiled into machine code that is executed directly by your system’s processor.) The `awk` utility is thus termed an *interpreter*. Many modern languages are interpreted.



An **associative array**, **map**, **symbol table**, or **dictionary** is an abstract data type composed of a collection (computing) of (key, value) pairs, such that each possible key appears at most once in the collection.



#### Reference

1. how to [load file into array](<https://magvar.wordpress.com/2011/05/18/awking-it-how-to-load-a-file-into-an-array-in-awk/>) in akw

2. [awk tutorial](<http://www.grymoire.com/Unix/Awk.html#uh-23>) with example scripts
3. wiki page [awk](<https://en.wikipedia.org/wiki/AWK>)
