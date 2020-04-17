TOPNAME=jtagpwm_tinyfpga_bx
CLASSNAME=JtagPwm_TinyFPGA_BX
PCF=tinyfpga_bx.pcf
SRC=JtagPwm_TinyFPGA_BX.scala PwmCtrl.scala
ICEVIEW=$(HOME)/Dropbox/monthly/201912/fpga/ice40_viewer/iceview_html.py

$(TOPNAME).asc: $(TOPNAME).json $(PCF)
	nextpnr-ice40 \
		--lp8k \
		--package cm81 \
		--asc $@ \
		--pcf $(PCF) \
		--json $(TOPNAME).json

$(TOPNAME).json: $(CLASSNAME).v
	yosys \
		-ql yosys.log \
		-p 'synth_ice40 -top '$(CLASSNAME)' -json '$@ \
		$^

$(CLASSNAME).v: $(addprefix src/main/scala/, $(SRC))
	sbt "runMain $(CLASSNAME)"

$(TOPNAME).html: $(TOPNAME).asc
	$(ICEVIEW) $(TOPNAME).asc $@

html: $(TOPNAME).html

timeanal:
	icetime -tmd lp8k $(TOPNAME).asc

$(TOPNAME).bin: $(TOPNAME).asc
	icepack $(TOPNAME).asc $@

upload: $(TOPNAME).bin
	tinyprog -p $^

clean:
	@rm -f $(TOPNAME).asc $(TOPNAME).json $(CLASSNAME).v yosys.log \
		verbose.log
