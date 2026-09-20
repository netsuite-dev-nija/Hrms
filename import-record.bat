@echo off

for %%i in (
customrecord_hris_employee_compen_change
customrecord_hris_leavebalance
customrecord_hris_employeedatasourcing
customrecord_hris_employee_compensation
customrecord_hris_leaveconfig
customrecord_hris_unique_reference_numbe
customrecord_hris_holiday_master
customrecord_cseg_njt_seg_emp
customrecord_hris_employeedatasourcing
) do (
    suitecloud object:import --type ALL --destinationfolder "/Objects" --scriptid %%i
)