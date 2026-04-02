CREATE STREAM IF NOT EXISTS EMPLOYEE_EVENTS_JSON (
    action VARCHAR,
    empId INTEGER,
    name VARCHAR,
    deptId INTEGER,
    salary DOUBLE,
    eventTimestamp VARCHAR
) WITH (
    KAFKA_TOPIC = 'employee-events',
    VALUE_FORMAT = 'JSON_SR'
);

SELECT action, empId, name, deptId, salary, eventTimestamp
FROM EMPLOYEE_EVENTS_JSON
EMIT CHANGES;
