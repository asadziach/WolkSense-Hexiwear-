//
//  Hexiwear application is used to pair with Hexiwear BLE devices
//  and send sensor readings to WolkSense sensor data cloud
//
//  Copyright (C) 2016 WolkAbout Technology s.r.o.
//
//  Hexiwear is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  Hexiwear is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//
//  ShowDevicesTableViewController.swift
//

import UIKit

protocol ActivatedDeviceDelegate {
    func didSelectAlreadyActivatedName(name: String, serial: String)
}

class ShowDevicesTableViewController: UITableViewController {
    var activatedDeviceDelegate: ActivatedDeviceDelegate?
    
    var selectableDevices: [Device] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Continue device"
    }
    
    // MARK: - Table view data source
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return selectableDevices.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("deviceCell", forIndexPath: indexPath)
        let device = selectableDevices[indexPath.row]
        cell.textLabel?.text = device.name
        cell.detailTextLabel?.text = device.deviceSerial
        cell.accessoryType = .None
        
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let device = selectableDevices[indexPath.row]
        activatedDeviceDelegate?.didSelectAlreadyActivatedName(device.name, serial: device.deviceSerial)
    }
    
    override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 60.0
    }

}
