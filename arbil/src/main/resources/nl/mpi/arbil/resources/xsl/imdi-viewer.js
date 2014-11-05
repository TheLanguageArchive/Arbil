/*
 * View IMDI 3.0 as HTML
 * 
 * File  : imdi_viewer.js
 * Author: fredof
 * Date  : June 9, 2006
 *  
 * Copyright (C) 2006  Freddy Offenga <freddy.offenga@mpi.nl>
 * Max Planck Institute for Psycholinguistics
 * Wundtlaan 1, 6525 XD Nijmegen, The Netherlands
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

var IMG_OPEN = '';
var IMG_CLOSED = '';

/*
 * init. viewer functions
 */
function init_viewer(o_img,c_img) {
  IMG_OPEN = o_img;
  IMG_CLOSED = c_img;
  //alert(IMG_OPEN + "," + IMG_CLOSED);
}

/*
 * check to see if an element with a given id is open
 */
function is_open(id) {
  var GECKO = document.getElementById ? 1:0 ;
  var NS = document.layers ? 1:0 ;
  var IE = document.all ? 1:0 ;
  var opened = 0;
           
  if (GECKO) {
    opened = (document.getElementById(id).style.display=='block');
  }
  else if (NS) {
    opened = (document.layers[id].display=='block');
  }
  else if (IE) {
    opened = (document.all[id].style.display=='block');
  }
  return opened;
}

function get_element(id) {
  var GECKO = document.getElementById ? 1:0 ;
  var NS = document.layers ? 1:0 ;
  var IE = document.all ? 1:0 ;
	
  var elem = null;
	
  if (GECKO) {
    elem = document.getElementById(id); 
  }
  else if (NS) {
    elem = document.layers[id];
  }
  else if (IE) {
    elem = document.all[id];
  }
  	
  return elem;
}

/*
 * change status of open/closed group
 */
function change_status(id) {
  var GECKO = document.getElementById ? 1:0 ;
  var NS = document.layers ? 1:0 ;
  var IE = document.all ? 1:0 ;

  var status;
  var img_src;
	
  if (is_open(id)) {
    status = 'none';
    img_src = IMG_CLOSED;
  }
  else {
    status = 'block';
    img_src = IMG_OPEN;	    
  }
            
  if (GECKO) {
    document.getElementById(id).style.display = status;	
  }
  else if (NS) {
    document.layers[id].display = status;
  }
  else if (IE) {
    document.all[id].style.display = status;
  }
	
  var img_id = 'img_' + id;
  var img_elem = get_element(img_id);
  img_elem.src = img_src;
}
