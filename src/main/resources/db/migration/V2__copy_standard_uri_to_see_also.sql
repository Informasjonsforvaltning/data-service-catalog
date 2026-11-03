-- Standards (conformsTo) can have several links. The new `seeAlso` list replaces
-- `uri`; existing values are copied into it as a list with one element.
CREATE FUNCTION pg_temp.standards_with_see_also(standards jsonb) RETURNS jsonb AS $$
SELECT COALESCE(
    jsonb_agg(
        CASE
            WHEN jsonb_typeof(standard -> 'uri') = 'string'
                AND standard ->> 'uri' <> ''
                AND NOT standard ? 'seeAlso'
                THEN standard || jsonb_build_object('seeAlso', jsonb_build_array(standard ->> 'uri'))
            ELSE standard
        END
        ORDER BY position
    ),
    '[]'::jsonb
)
FROM jsonb_array_elements(standards) WITH ORDINALITY AS t(standard, position)
$$ LANGUAGE sql IMMUTABLE;

UPDATE data_services
SET data = jsonb_set(data, '{conformsTo}', pg_temp.standards_with_see_also(data -> 'conformsTo'))
WHERE jsonb_typeof(data -> 'conformsTo') = 'array';
