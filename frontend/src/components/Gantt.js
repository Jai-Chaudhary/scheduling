const React = require('react');
const lodash = require('lodash');
const d3 = require('d3');
const d3tip = require('d3-tip');
require('d3-tip/examples/example-styles.css');

const rowHeight = 30;
const bandHeight = 20;
const width = '100%';
const padding = {
  top: 90,
  right: 40,
  bottom: 35,
  left: 180
};
const rx = 5;
const strokeWidth = 3;
const textXOffset = 5;
const textFontSize = 14;

export default React.createClass({
  propTypes: {
    data: React.PropTypes.array.isRequired,
    begin: React.PropTypes.func.isRequired,
    end: React.PropTypes.func.isRequired,
    row: React.PropTypes.func.isRequired,
    color: React.PropTypes.func,
    xlim: React.PropTypes.array,
    stroke: React.PropTypes.func,
    text: React.PropTypes.func,
    tip: React.PropTypes.func,
    time: React.PropTypes.any,
  },
  componentDidMount() {
    const tip = d3tip().attr('class', 'd3-tip');
    d3.select(this.getDOMNode()).call(tip);
    this.tip = tip;

    this.draw();
  },
  componentWillUnmount() {
    this.removeDraw();
    this.tip.destroy();
  },
  componentDidUpdate() {
    this.removeDraw();
    this.draw();
  },
  render() {
    const style = lodash.extend({
      width: width
    }, this.props.style);
    return <svg style={style}></svg>;
  },
  removeDraw() {
    this.rects.on('mouseover', null)
    .on('mouseout', null);
    this.container.remove();
  },
  draw() {
    const data = this.props.data;
    const begin = this.props.begin;
    const end = this.props.end;
    const row = this.props.row;

    let container;
    let xScale;
    let yScale;
    let height;

    prepare.call(this);
    drawRect.call(this);
    drawAxis();
    drawTime.call(this);

    function prepare() {
      const element = this.getDOMNode();

      let xlim = [
        lodash.min(data.map(begin)),
        lodash.max(data.map(end))
      ];
      if (this.props.xlim) xlim = this.props.xlim;

      xScale = d3.time.scale().domain(xlim).range([
        0, element.clientWidth - padding.right - padding.left
      ]);

      const rows = lodash.unique(data.map(row)).sort();
      height = rows.length * rowHeight;

      yScale = d3.scale.ordinal().domain(rows)
      .range(lodash.range(0, height, rowHeight));

      const svg = d3.select(element);
      svg.attr("height", height + padding.top + padding.bottom);

      container = svg.append('g')
      .attr("transform", `translate(${padding.left}, ${padding.top})`);
      this.container = container;
    }

    function drawRect() {
      const color = this.props.color ? this.props.color : () => 'steelblue';
      const stroke = this.props.stroke ? this.props.stroke : () => 'none';

      const rects = container.append("g").selectAll("rect")
      .data(data).enter().append("rect")
      .attr("rx", rx)
      .attr("stroke", stroke)
      .attr("stroke-width", strokeWidth)
      .attr("width", (d) => xScale(end(d)) - xScale(begin(d)))
      .attr("height", bandHeight)
      .attr("x", (d) => xScale(begin(d)))
      .attr("y", (d) => yScale(row(d)))
      .attr("fill", (d) => color(d));
      this.rects = rects;

      if (this.props.tip) {
        const tip = this.tip;
        tip.html(this.props.tip);
        rects.on('mouseover', tip.show)
        .on('mouseout', tip.hide);
      }

      if (this.props.text) {
        container.append("g").selectAll('text')
        .data(data).enter().append('text')
        .attr('x', d => xScale(begin(d)) + textXOffset)
        .attr('y', d => yScale(row(d)) + rowHeight/2)
        .style('font-size', textFontSize)
        .text(d => this.props.text(d));
      }
    }

    function drawAxis() {
      const xAxis = d3.svg.axis().scale(xScale).orient('bottom')
      .tickFormat(d3.time.format("%H:%M"))
      .tickPadding(8);
      const yAxis = d3.svg.axis().scale(yScale).orient("left").tickSize(0);

      const tmp = container.append('g').attr("class", "x axis")
      .attr("transform", `translate(0,${height})`).call(xAxis);
      tmp.selectAll('line').style('stroke', '#000');
      tmp.selectAll('path')
      .style('fill', 'none')
      .style('stroke', '#000');

      container.append('g')
      .attr("transform", `translate(0, ${bandHeight/2})`).call(yAxis);
    }

    function drawTime() {
      if (!this.props.time) return;

      const x = xScale(this.props.time);
      container.append('line')
      .attr("x1", x).attr("x2", x)
      .attr("y1", 0).attr("y2", height)
      .style("stroke", "#000")
      .style("stroke_width", 2);
    }

  }
});
