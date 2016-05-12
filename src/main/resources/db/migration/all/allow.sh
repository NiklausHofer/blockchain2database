#!/bin/bash
files=$(ls V1.*.sql)

> V3.02__grant_select_on_tables_to_pubweb.sql
> V3.05__grant_select_on_tables_to_privweb.sql

for file in ${files}
do
  # skip the V1.9X files
  if [ "${file}" \> "V1.9" ]; then
    continue
  fi

  table=$(perl -n -e'/create\s+table\s+(?:if\s+not\s+exists)?\s+(\w+)/i && print $1' ${file})
  if [ "${table}" != "" ]
  then
    echo "-- table ${table} created in file ${file}" >> V3.02__grant_select_on_tables_to_pubweb.sql
    echo "GRANT SELECT ON TABLE ${table} TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME \${MAX_QUERY_TIME};" >> V3.02__grant_select_on_tables_to_pubweb.sql

    echo "-- table ${table} created in file ${file}" >> V3.05__grant_select_on_tables_to_privweb.sql
    echo "GRANT SELECT ON TABLE ${table} TO 'privweb'@'localhost';" >> V3.05__grant_select_on_tables_to_privweb.sql
  fi

  view=$(perl -n -e'/create\s+view\s+(\w+)/i && print $1' ${file})
  if [ "${view}" != "" ]
  then
    echo "-- view ${view} created in file ${file}" >> V3.02__grant_select_on_tables_to_pubweb.sql
    echo "GRANT SELECT, SHOW VIEW ON TABLE ${view} TO 'pubweb'@'localhost' WITH MAX_STATEMENT_TIME \${MAX_QUERY_TIME};" >> V3.02__grant_select_on_tables_to_pubweb.sql

    echo "-- view ${view} created in file ${file}" >> V3.05__grant_select_on_tables_to_privweb.sql
    echo "GRANT SELECT, SHOW VIEW ON TABLE ${view} TO 'privweb'@'localhost';" >> V3.05__grant_select_on_tables_to_privweb.sql
  fi

done
