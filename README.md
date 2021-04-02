# MetaMap Med Term Frequency Builder

Building medical term dictionary with word frequency using metamap.

## MetaMap Installation

https://metamap.nlm.nih.gov/download/public_mm_linux_main_2020.tar.bz2

https://metamap.nlm.nih.gov/download/public_mm_linux_javaapi_2020.tar.bz2

Copy all files from `javaapi/public_mm/bin` to `public_mm_linux_main_2020/public_mm/bin`

Copy `javaapi/public_mm/bin/mmserver` to `public_mm_linux_main_2020/public_mm`

To install server, run inside `public_mm`

`bin/instal.sh`

## MetaMap Server Run

Set working directory `export $MED_FREQUENCY_WORKDIR=/YOURS_WORKDIR`

Copy files from this repo `./scripts` folder to `public_mm_linux_main_2020/public_mm`

`chmod +x start.sh stop.sh`

Replace absolute path for metamap `public_mm` folder

To run all servers execute `./start.sh` (mmserver std output will start in terminal)

To stop all servers execute `./stop.sh`

For install service run

`./installMetamapService.sh`

## Example Execution

Provide spring active profile equal to `example`

```shell
export SPRING_PROFILES_ACTIVE=example
```

Example will show MetaMap results for hardcoded piece of medical document.

## MIMIC III NoteEvents Execution

Unset `example` spring active profile if set.

```shell
unset SPRING_PROFILES_ACTIVE
```

Provide path to noteevents file

```shell
export APP_NOTEEVENTS=file:"$MED_FREQUENCY_WORKDIR"/noteevents.csv
```

By default noteevents path set to 

```yaml
app:
  noteEvents: file:${MED_FREQUENCY_WORKDIR}/noteevents.csv
```
