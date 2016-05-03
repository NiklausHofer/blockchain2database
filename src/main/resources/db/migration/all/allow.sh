#!/bin/bash
files=$(ls V1.*.sql)

> V3.02__grant_select_on_tables_to_pubweb.sql
> V3.05__grant_select_on_tables_to_privweb.sql

for file in ${files}
do
  table=$(perl -n -e'/create\s+table\s+(?:if\s+not\s+exists)?\s+(\w+)/i && print $1' ${file})
  echo "-- table ${table} created in file ${file}" >> V3.02__grant_select_on_tables_to_pubweb.sql
  echo "GRANT SELECT, SHOW VIEW ON TABLE ${table} TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME \${MAX_QUERY_TIME};" >> V3.02__grant_select_on_tables_to_pubweb.sql

  echo "-- table ${table} created in file ${file}" >> V3.05__grant_select_on_tables_to_privweb.sql
  echo "GRANT SELECT, SHOW VIEW ON TABLE ${table} TO 'privweb'@'localhost';" >> V3.05__grant_select_on_tables_to_privweb.sql
done
