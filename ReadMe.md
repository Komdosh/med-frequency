# MetaMap Med Term Frequency Builder

Building medical term dictionary with word frequency using metamap.

## MetaMap Installation

https://metamap.nlm.nih.gov/download/public_mm_linux_main_2020.tar.bz2

https://metamap.nlm.nih.gov/download/public_mm_linux_javaapi_2020.tar.bz2

copy all files from `javaapi/public_mm/bin` to `public_mm_linux_main_2020/public_mm/bin`

copy `javaapi/public_mm/bin/mmserver` to `public_mm_linux_main_2020/public_mm`

To install server, run inside `public_mm`

`bin/instal.sh`

## MetaMap Server Run

copy files from this repo `./scripts` folder to `public_mm_linux_main_2020/public_mm`

`chmod +x start.sh stop.sh`

replace absolute path for metamap `public_mm` folder

to run all servers execute `./start.sh` (mmserver std output will start in terminal)

to stop all servers execute `./stop.sh`

for install service run

`./installMetamapService.sh`

## Example Execution

Provide spring active profile equal to `example` into application.yml

```yaml
spring:
  profiles:
    active: example
```

## MIMIC III NoteEvents Execution

Delete `example` spring active profile if set.

Provide path to noteevents file into application.yml

```yaml
app:
  noteEvents: file:pathToNoteEvents.csv
```
